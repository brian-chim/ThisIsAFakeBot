package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class ImportYTPCommand extends CommandAbstract {
	
	private VoiceChannel vc;
	
	public ImportYTPCommand() {
		commandHandled = "importytp";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 2) {
			throw new IncorrectUsageException();
		}
		MessageChannel channel = input.getChannel();
		GuildVoiceState vs = input.getMember().getVoiceState();
		boolean properUsage = false;
		if (vs != null) {
			if (vs.getChannel() != null) {
				properUsage = true;
			}
		}
		if (!properUsage) {
			String msg = "You must be in a voice channel to use that!";
			channel.sendMessage(msg).queue();
			throw new IncorrectUsageException();
		}
		this.vc = vs.getChannel();
	    TextChannel textChannel = input.getTextChannel();
		String[] vids = tokenized[1].split(";");
		for (String vidId : vids) {
			loadAndPlay(textChannel, "https://www.youtube.com/watch?v=" + vidId);
		}
		String msg = "Adding " + vids.length + " songs to queue. The list may not update right away.";
		channel.sendMessage(msg).queue();
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
	    channel.sendMessage(msg).queue();
	}
	
	private void loadAndPlay(final TextChannel channel, final String trackUrl) {
		MusicHandler mh = MusicHandler.getInstance();
		GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
		
		mh.getAudioPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
	        	play(channel.getGuild(), musicManager, track);
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
		        if (firstTrack == null) {
		          firstTrack = playlist.getTracks().get(0);
		        }
		        play(channel.getGuild(), musicManager, firstTrack);
		      }
			@Override
			public void noMatches() {
				channel.sendMessage("Nothing found by " + trackUrl).queue();
			}
			@Override
		    public void loadFailed(FriendlyException exception) {
		    	channel.sendMessage("Could not play: " + exception.getMessage()).queue();
		    }
	    });
	}

	private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
		guild.getAudioManager().openAudioConnection(vc);
		musicManager.scheduler.queue(track);
	}
}
