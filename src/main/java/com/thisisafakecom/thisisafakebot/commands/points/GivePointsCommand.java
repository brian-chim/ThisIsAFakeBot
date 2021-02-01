package com.thisisafakecom.thisisafakebot.commands.points;

import java.util.List;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.NegativePointsException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class GivePointsCommand extends CommandAbstract {

  public GivePointsCommand() {
    commandHandled = "givepoints";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if(tokenized.length != 3) {
      throw new IncorrectUsageException();
    } else {
      MessageChannel channel = input.getChannel();
      // check for actual number of points given
      int numPointsToGive = 0;
      try {
    	  numPointsToGive = Integer.parseInt(tokenized[2]);
      } catch (NumberFormatException e) {
    	  throw new IncorrectUsageException();
      }
      // check for exactly one user
      List<User> users = input.getMentionedUsers();
      if (users.size() == 0) {
    	  channel.sendMessage("You have to specify a user to give points to!").queue();
    	  return;
      } else if (users.size() > 1) {
    	  channel.sendMessage("You can only give points to one user at a time!").queue();
    	  return;
      }
      User giver = input.getAuthor();
      // check that the user has enough points to give to that person
      if (PointsDBHandler.getPoints(giver) < numPointsToGive) {
    	  channel.sendMessage("You don't have enough points to do that.").queue();
    	  return;
      } else {
    	  try {
    		  PointsDBHandler.removePoints(giver, numPointsToGive);
    	  } catch (NegativePointsException e) {
    		  // should not occur because of earlier check
    		  e.printStackTrace();
    		  channel.sendMessage("Something went wrong!");
    		  return;
    	  }
    	  PointsDBHandler.addPoints(users.get(0), numPointsToGive);
      }
      String msg = "You gave " + numPointsToGive + " points to " + users.get(0).getAsMention() + "!";
      channel.sendMessage(msg).queue();
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " [@User] [# of Points]"+ "``";
    channel.sendMessage(msg).queue();
  }

@Override
public String commandDescription() {
	String ret = "Gives a user a number of points.\n"
			+ "Usage: " + App.botPrefix + commandHandled + " [@User] [# of Points]";
	return ret;
}

}
