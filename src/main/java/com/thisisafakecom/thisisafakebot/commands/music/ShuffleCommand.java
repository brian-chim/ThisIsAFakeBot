package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class ShuffleCommand extends CommandAbstract {

  public ShuffleCommand() {
    commandHandled = "shuffle";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1) {
      throw new IncorrectUsageException();
    } else {
      TextChannel channel = input.getTextChannel();
      MusicHandler mh = MusicHandler.getInstance();
      GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
      musicManager.scheduler.shuffleQueue();
      channel.sendMessage("The queue has been shuffled!").queue();
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
    String ret = "Shuffles the music queue.\n" + "Usage: " + App.botPrefix + commandHandled;
    return ret;
  }
}
