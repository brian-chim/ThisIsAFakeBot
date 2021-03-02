package com.thisisafakecom.thisisafakebot.commands.music.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
// Credit: https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/TrackScheduler.java
public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private final BlockingDeque<AudioTrack> queue;
  private AudioTrack currentTrack;
  private boolean loop = false;
  
  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(AudioPlayer player) {
    this.player = player;
    this.queue = new LinkedBlockingDeque<>();
  }

  /**
   * Add the next track to queue or play right away if nothing is in the queue.
   *
   * @param track The track to play or add to queue.
   */
  public void queue(AudioTrack track) {
    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
    // something is playing, it returns false and does nothing. In that case the player was already playing so this
    // track goes to the queue instead.
    if (!player.startTrack(track, true)) {
      queue.offer(track);
    } else {
      currentTrack = track;
    }
  }

  public void queueNow(AudioTrack track) {
      if (currentTrack != null) {
        queue.offerFirst(currentTrack.makeClone());
      }
      currentTrack = track;
      player.startTrack(track, false);
  }

  /**
   * Start the next track, stopping the current one if it is playing.
   */
  public void nextTrack() {
    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
    // giving null to startTrack, which is a valid argument and will simply stop the player.
	this.currentTrack = queue.poll();
    player.startTrack(currentTrack, false);
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
    if (endReason.mayStartNext) {
      if (loop && endReason != AudioTrackEndReason.LOAD_FAILED) {
         queue.offer(track.makeClone());
      }
      nextTrack();
    }
  }

  public AudioTrack[] getNextTracks(int max) {
	  AudioTrack[] ret = new AudioTrack[max];
	  if (currentTrack != null) {
		  ret[0] = currentTrack;
	  }
	  Iterator <AudioTrack> it = queue.iterator();
	  int i = 1;
	  while (it.hasNext() && i < max) {
		  ret[i] = it.next();
		  i++;
	  }
	  return ret;
  }

  public ArrayList<AudioTrack> getAllTracks() {
	  ArrayList<AudioTrack> ret = new ArrayList<AudioTrack>();
	  if (currentTrack != null) {
		  ret.add(currentTrack);
	  }
	  Iterator <AudioTrack> it = queue.iterator();
	  while (it.hasNext()) {
		  ret.add(it.next());
	  }
	  return ret;
  }
  public int clearQueue() {
	  int removedCount = queue.size();
	  queue.clear();
    return (currentTrack != null ? removedCount + 1 : removedCount);
  }

  public AudioTrack getCurrentTrack() {
	  return currentTrack;
  }

  public void setLoop(boolean loop) {
	  this.loop = loop;
  }

  public boolean getLoop() {
	  return loop;
  }
  
  public int getNumSongsLeft() {
	  return (currentTrack != null ? queue.size() + 1 : queue.size());
  }
  
  public void shuffleQueue() {
	  List<AudioTrack> temp = new ArrayList<AudioTrack>();
	  queue.drainTo(temp);
	  Collections.shuffle(temp);
	  queue.addAll(temp);
  }
  
  public void removeRangeFromQueue(int start, int end) {
    end = end > queue.size() + 1 ? queue.size() + 1 : end;
    if (start <= end) {
      List<AudioTrack> temp = new ArrayList<AudioTrack>();
      List<AudioTrack> newQueue = new ArrayList<AudioTrack>();
      queue.drainTo(temp);
      // note need to offset by 2 because currTrack is not stored in the actual queue and user is off by one
      for (int i = 0; i < start - 2; i++) {
        newQueue.add(temp.get(i));
      }
      for (int j = end - 1; j < temp.size(); j++) {
        newQueue.add(temp.get(j));
      }
      queue.addAll(newQueue);
    }
  }
}