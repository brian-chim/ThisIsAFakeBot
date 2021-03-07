package com.thisisafakecom.thisisafakebot.commands.music.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
// Credit: https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/GuildMusicManager.java
public class GuildMusicManager {
  /**
   * Audio player for the guild.
   */
  public final AudioPlayer player;
  /**
   * Track scheduler for the player.
   */
  public final TrackScheduler scheduler;

  /**
   * Creates a player and a track scheduler.
   * 
   * @param manager Audio player manager to use for creating the player.
   */
  public GuildMusicManager(AudioPlayerManager manager) {
    player = manager.createPlayer();
    scheduler = new TrackScheduler(player, manager);
    player.addListener(scheduler);
  }

  /**
   * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
   */
  public AudioPlayerSendHandler getSendHandler() {
    return new AudioPlayerSendHandler(player);
  }
}