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
		AudioTrack[] tracks = gm.scheduler.getLatest5Tracks();
		String msg = "";
		boolean containsInfo = false;
		if (tracks[0] != null) {
			containsInfo = true;
		}
		for (AudioTrack track : tracks) {
			if (track == null) {
				break;
			}
			msg += MusicUtils.getTrackTitleAndLength(track) + "\n";
		}
		if (containsInfo) {
			msg = "``" + msg + "``";
		}
		if (msg.isEmpty()) {
			msg = "No tracks in queue!";
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
}