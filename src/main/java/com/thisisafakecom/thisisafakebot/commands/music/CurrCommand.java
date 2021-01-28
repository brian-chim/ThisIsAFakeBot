package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class CurrCommand extends CommandAbstract {

	public CurrCommand() {
		commandHandled = "curr";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		}
		MessageChannel channel = input.getChannel();
		Guild guild = input.getGuild();
		AudioTrack currentTrack = MusicHandler.getInstance().getGuildAudioPlayer(guild).scheduler.getCurrentTrack();
		String msg = "``" + MusicUtils.getTrackTitleAndLength(currentTrack) + "``";
		channel.sendMessage(msg).queue();;
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}

	@Override
	public String commandDescription() {
		String ret = "Shows the currently playing song.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}

}
