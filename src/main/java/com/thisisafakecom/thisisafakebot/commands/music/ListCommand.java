package com.thisisafakecom.thisisafakebot.commands.music;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicUtils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;

public class ListCommand extends CommandAbstract {

  int curr = 0;
  String leftArrowUnicode = "U+2b05";
  String rightArrowUnicode = "U+27a1";
  String arrowUnicode = leftArrowUnicode + rightArrowUnicode;

	public ListCommand() {
		commandHandled = "list";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		}
		MusicHandler mh = MusicHandler.getInstance();
		GuildMusicManager gm = mh.getGuildAudioPlayer(input.getGuild());
		List<AudioTrack> allTracks = gm.scheduler.getAllTracks();
		curr = 0;
		int numPages = allTracks.size() / 5;
		if (allTracks.size() % 5 != 0) {
		  numPages += 1;
		}
    MessageChannel channel = input.getChannel();
    if (numPages == 0) {
      channel.sendMessage("No tracks in queue!").queue();
      return;
    } else {
      String msg = "Page " + (curr + 1) + " of " + numPages + "\n";
      int counter = numPages == 1 ? allTracks.size() : 5;
      for (int i = 0; i < counter; i++) {
        msg += i+1 + ". " + MusicUtils.getTrackTitleAndLength(allTracks.get(i)) + "\n";
      }
      msg += "There is a total of " + gm.scheduler.getNumSongsLeft() + " songs left in the queue.";
      msg += gm.scheduler.getLoop() ? "\n\nLoop is currently enabled." : "";
      msg += gm.scheduler.getUnending() ? "\n\nPlaylist is currently on unending mode." : "";
      msg = "```" + msg + "```";
      Message msgCallback = channel.sendMessage(msg).complete();
      if (numPages > 1) {
        msgCallback.addReaction(leftArrowUnicode).queue();
        msgCallback.addReaction(rightArrowUnicode).queue();
        long id = msgCallback.getIdLong();
        waitForUser(input, id, msgCallback, allTracks, numPages, gm); 
      }
      return;
    }
	}

	 public void waitForUser(Message input, long reactMsgId, Message msgCallback, List<AudioTrack> allTracks, int numPages, GuildMusicManager gm) {
	    App.waiter.waitForEvent(GenericGuildMessageReactionEvent.class,
	        e -> e.getUser().equals(input.getAuthor())
	        && e.getReaction().getChannel().equals(input.getChannel())
	        && arrowUnicode.contains(e.getReactionEmote().getAsCodepoints())
	        && e.getMessageIdLong() == reactMsgId,
	        e -> {
	            if (e.getReactionEmote().getAsCodepoints().equals(rightArrowUnicode)) {
	              // increment
	              curr = curr == numPages - 1 ? 0 : curr + 1;
	            } else {
	              // decrement
	              curr = curr == 0 ? numPages - 1 : curr - 1;
	            }
	            msgCallback.clearReactions().queue();
	            String edit = "Page " + (curr + 1) + " of " + numPages + "\n";
	            // either the last page which has less than 5, or just 5 per page
	            int counter = curr == numPages - 1 && allTracks.size() % 5 != 0 ? allTracks.size() % 5 : 5;
	            for (int i = 0; i < counter; i++) {
	              edit += (curr * 5 + i + 1) + ". " + MusicUtils.getTrackTitleAndLength(allTracks.get(curr * 5 + i)) + "\n";
	            }
	            edit += "There is a total of " + gm.scheduler.getNumSongsLeft() + " songs left in the queue.";
	            edit += gm.scheduler.getLoop() ? "\n\nLoop is currently enabled." : "";
	            edit = "```" + edit + "```";
	            msgCallback.editMessage(edit).queue();
	            msgCallback.addReaction(leftArrowUnicode).queue();
	            msgCallback.addReaction(rightArrowUnicode).queue();
	            waitForUser(input, reactMsgId, msgCallback, allTracks, numPages, gm);
	          },
	        30, TimeUnit.SECONDS, () -> {});
	  }

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}

	@Override
	public String commandDescription() {
		String ret = "Lists the next 5 songs with their name and the total number of songs in the playlist.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}
}
