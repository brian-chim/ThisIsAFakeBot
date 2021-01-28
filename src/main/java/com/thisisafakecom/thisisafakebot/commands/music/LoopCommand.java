package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class LoopCommand extends CommandAbstract {

	public LoopCommand() {
		commandHandled = "loop";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		} else {
			Guild guild = input.getGuild();
			MusicHandler mh = MusicHandler.getInstance();
			GuildMusicManager gm = mh.getGuildAudioPlayer(guild);
			gm.scheduler.setLoop(!gm.scheduler.getLoop());
			String msg = "Playlist Loop has been " + (gm.scheduler.getLoop() ? "enabled" : "disabled");
		    MessageChannel channel = input.getChannel();
			channel.sendMessage(msg).queue();
		}
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}

	@Override
	public String commandDescription() {
		String ret = "Set the bot to loop / stop looping through your music.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}
}
