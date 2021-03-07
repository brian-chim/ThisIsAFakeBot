package com.thisisafakecom.thisisafakebot.commands.points;

import java.util.List;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class GetPointsCommand extends CommandAbstract {

  public GetPointsCommand() {
    commandHandled = "points";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1 && tokenized.length != 2) {
      throw new IncorrectUsageException();
    } else {
      String msg = "";
      MessageChannel channel = input.getChannel();
      if (tokenized.length == 1) {
        User author = input.getAuthor();
        int points = PointsDBHandler.getPoints(author);
        msg = author.getName() + ": You currently have " + points + " points.";
      } else {
        List<User> mentioned = input.getMentionedUsers();
        if (mentioned.size() != 1) {
          msg = "You can only check the points of one user at a time!";
        } else {
          User check = mentioned.get(0);
          int points = PointsDBHandler.getPoints(check);
          msg = check.getName() + " currently has " + points + " points.";
        }
      }
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
    String ret = "Lists the number of points you have.\n" + "Usage: " + App.botPrefix + commandHandled;
    return ret;
  }

}
