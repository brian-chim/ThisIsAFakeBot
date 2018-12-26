package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class PlayVoiceCommand extends CommandAbstract {

  PlayVoiceCommand() {
    this.commandHandled = "play";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length == 2) {
      Member user = input.getMember();
      VoiceChannel channelToJoin = user.getVoiceState().getChannel();
      if (channelToJoin == null) {
        // send message saying you needa be in voice
        System.out.println("not in voice");
      } else {
        App.playerManager.loadItem(tokenized[1], new AudioLoadResultHandler() {
          @Override
          public void trackLoaded(AudioTrack track) {
            App.trackScheduler.queue(track);
          }

          @Override
          public void playlistLoaded(AudioPlaylist playlist) {
            for (AudioTrack track : playlist.getTracks()) {
              App.trackScheduler.queue(track);
            }
          }

          @Override
          public void noMatches() {
            // Notify the user that we've got nothing
          }

          @Override
          public void loadFailed(FriendlyException throwable) {
            // Notify the user that everything exploded
          }
        });
        // then play the song
        App.player.playTrack(App.trackScheduler.getTrackSchedule().get(0));
      }
    } else {
      throw new IncorrectUsageException();
    }
    
  }

  @Override
  public void correctUsage(Message input) {
    System.out.println("something went wrong");
  }

}
