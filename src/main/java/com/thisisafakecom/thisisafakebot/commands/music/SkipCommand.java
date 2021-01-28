package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class SkipCommand extends CommandAbstract {

	public SkipCommand() {
		commandHandled = "skip";
	}
	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		} else {
			skipTrack(input.getTextChannel());
		    input.getChannel().sendMessage("Skipped to next track.").queue();
		}
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}

	private void skipTrack(TextChannel channel) {
		MusicHandler mh = MusicHandler.getInstance();
	    GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
	    musicManager.scheduler.nextTrack();
	}
	@Override
	public String commandDescription() {
		String ret = "Skips the currently playing song.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}
}
