package com.thisisafakecom.thisisafakebot;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.thisisafakecom.thisisafakebot.commands.CommandHandler;
import com.thisisafakecom.thisisafakebot.commands.CommandNotSupportedException;
import com.thisisafakecom.thisisafakebot.commands.music.TrackScheduler;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class App extends ListenerAdapter 
{

  private final String botPrefix = "!";
  private final CommandHandler ch = new CommandHandler();
  public static AudioPlayerManager playerManager;
  public static AudioPlayer player;
  public static TrackScheduler trackScheduler;
  
  public static void main( String[] args ) throws LoginException {
    JDABuilder bot = new JDABuilder(AccountType.BOT);
    bot.setToken("token");
    // add all the audio stuff
    playerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(playerManager);
    player = playerManager.createPlayer();
    trackScheduler = new TrackScheduler();
    player.addListener(trackScheduler);
    
    bot.addEventListener(new App());
    bot.build();
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if(!event.getAuthor().isBot()) {
      Message msgReceived = event.getMessage();
      if (event.getMessage().getContentRaw().startsWith(botPrefix)) {
        try {
          ch.handleCommand(msgReceived);
        } catch (CommandNotSupportedException e) {
          event.getChannel().sendMessage("Command not found!").queue();
        }
      } 
    }
  }
}
