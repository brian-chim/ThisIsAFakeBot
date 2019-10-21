package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class PlayCommand extends CommandAbstract {

	private VoiceChannel vc;

	// Credit: https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/Main.java
	public PlayCommand() {
		commandHandled = "play";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
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
	    String[] tokenized = input.getContentRaw().split(" ");
	    TextChannel textChannel = input.getTextChannel();

		if (tokenized.length == 2) { // "!play url"
			loadAndPlay(textChannel, tokenized[1]);
			return;
		} else {
			throw new IncorrectUsageException();
		}
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " <url>``";
	    channel.sendMessage(msg).queue();
	}
	
	private void loadAndPlay(final TextChannel channel, final String trackUrl) {
		MusicHandler mh = MusicHandler.getInstance();
		GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
		
		mh.getAudioPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
	        	play(channel.getGuild(), musicManager, track);
			}

			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
		        if (firstTrack == null) {
		          firstTrack = playlist.getTracks().get(0);
		        }
		        channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
		        play(channel.getGuild(), musicManager, firstTrack);
		      }

			public void noMatches() {
				channel.sendMessage("Nothing found by " + trackUrl).queue();
			}

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
