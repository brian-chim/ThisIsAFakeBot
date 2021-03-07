package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class UnendingCommand extends CommandAbstract {

  public UnendingCommand() {
    commandHandled = "unending";
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
      MessageChannel channel = input.getChannel();
      if (gm.scheduler.getLoop()) {
        channel.sendMessage("You must turn off loop before turning on unending!").queue();
        return;
      }
      gm.scheduler.setUnending(!gm.scheduler.getUnending());
      String msg = "Unending playlist has been " + (gm.scheduler.getUnending() ? "enabled" : "disabled");
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
    String ret = "Set the bot to play / stop playing related songs to the last song in the queue.\n" + "Usage: "
        + App.botPrefix + commandHandled;
    return ret;
  }
}
