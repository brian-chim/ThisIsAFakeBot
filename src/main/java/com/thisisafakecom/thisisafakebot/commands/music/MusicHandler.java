package com.thisisafakecom.thisisafakebot.commands.music;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.api.entities.Guild;

public class MusicHandler {
	private static MusicHandler instance = null;
	private AudioPlayerManager playerManager;
	private Map<Long, GuildMusicManager> musicManagers;

	private MusicHandler() {
		playerManager = new DefaultAudioPlayerManager();
		musicManagers = new HashMap<>();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
	    GuildMusicManager musicManager = musicManagers.get(guildId);
	    if (musicManager == null) {
	    	musicManager = new GuildMusicManager(playerManager);
	    	musicManagers.put(guildId, musicManager);
		}
	    guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
	    return musicManager;
	}

	public AudioPlayerManager getAudioPlayerManager() {
		return playerManager;
	}

	public static MusicHandler getInstance() {
		if (instance == null) {
			instance = new MusicHandler();
		}
		return instance;
	}

}
