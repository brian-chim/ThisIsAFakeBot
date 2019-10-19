package com.thisisafakecom.thisisafakebot.commands.music;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
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

public class MusicCommand extends CommandAbstract {

	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;
	private VoiceChannel vc;

	// Credit: https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/Main.java
	public MusicCommand() {
		commandHandled = "music";
		playerManager = new DefaultAudioPlayerManager();
		musicManagers = new HashMap<>();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		GuildVoiceState vs = input.getMember().getVoiceState();
		boolean properUsage = false;
		if (vs != null) {
			if (vs.getChannel() != null) {
				properUsage = true;
			}
		}
		if (!properUsage) {
			throw new IncorrectUsageException();
		}
		this.vc = vs.getChannel();
	    String[] tokenized = input.getContentRaw().split(" ");
	    TextChannel textChannel = input.getTextChannel();
		if (tokenized.length == 3) { // "!music play url"
			if (tokenized[1].equalsIgnoreCase("play")) {
				loadAndPlay(textChannel, tokenized[2]);
				return;
			}
		} else if (tokenized.length == 2) { // "!music skip"
			if (tokenized[1].equalsIgnoreCase("skip")) {
				skipTrack(textChannel);
				return;
			}
		}
		throw new IncorrectUsageException();
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " play <url>``" + "\n``" + App.botPrefix + commandHandled + " skip``";
	    channel.sendMessage(msg).queue();
	}

	private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
	    GuildMusicManager musicManager = musicManagers.get(guildId);
	    if (musicManager == null) {
	    	musicManager = new GuildMusicManager(playerManager);
	    	musicManagers.put(guildId, musicManager);
		}

		    guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		    return musicManager;
		  }
	
	private void loadAndPlay(final TextChannel channel, final String trackUrl) {
		GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
		
		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
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

	private void skipTrack(TextChannel channel) {
	    GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
	    musicManager.scheduler.nextTrack();
	    channel.sendMessage("Skipped to next track.").queue();
	}
}
