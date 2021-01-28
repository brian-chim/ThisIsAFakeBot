package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicUtils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ListCommand extends CommandAbstract {

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
		AudioTrack[] tracks = gm.scheduler.getNextTracks(5);
		String msg = "";
		for (int i = 0; i < tracks.length; i++) {
			if (tracks[i] == null) {
				break;
			}
			msg += i+1 + ". " + MusicUtils.getTrackTitleAndLength(tracks[i]) + "\n";
		}
		if (msg.isEmpty()) {
			msg = "No tracks in queue!";
		} else {
			msg += "There is a total of " + gm.scheduler.getNumSongsLeft() + " songs left in the queue.";
			msg = "```" + msg + "```";
		}
		MessageChannel channel = input.getChannel();
		channel.sendMessage(msg).queue();
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
