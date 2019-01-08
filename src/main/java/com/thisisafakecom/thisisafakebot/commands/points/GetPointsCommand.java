package com.thisisafakecom.thisisafakebot.commands.points;

import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.DBHandler;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class GetPointsCommand extends CommandAbstract {

  public GetPointsCommand() {
    commandHandled = "points";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if(tokenized.length != 1) {
      throw new IncorrectUsageException();
    } else {
      MessageChannel channel = input.getChannel();
      int points = DBHandler.getPoints(input.getAuthor().getId());
      String msg = input.getAuthor().getName() + ": You currently have " + points + " points.";
      channel.sendMessage(msg).queue();
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``!" + commandHandled + "``";
    channel.sendMessage(msg).queue();
  }

}
