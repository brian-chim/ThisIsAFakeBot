package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class StopCommand extends CommandAbstract {

	public StopCommand() {
		commandHandled = "stop";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		}
		Guild guild = input.getGuild();
		GuildMusicManager gm = MusicHandler.getInstance().getGuildAudioPlayer(input.getGuild());
		// clear the queue
		int removedCount = gm.scheduler.clearQueue();
		String msg = "Removed " + removedCount + " tracks from queue!";
		MessageChannel channel = input.getChannel();
		channel.sendMessage(msg).queue();
		// skip the current playing song
		gm.scheduler.nextTrack();
		// then close the connection
		guild.getAudioManager().closeAudioConnection();
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}

	@Override
	public String commandDescription() {
		String ret = "Removes all songs from the playlist and stops the bot.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}

}
