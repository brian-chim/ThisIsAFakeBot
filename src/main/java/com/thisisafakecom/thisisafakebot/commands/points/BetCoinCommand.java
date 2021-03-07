package com.thisisafakecom.thisisafakebot.commands.points;

import java.util.Random;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.NegativePointsException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class BetCoinCommand extends CommandAbstract {

  public BetCoinCommand() {
    commandHandled = "betcoin";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 3) {
      throw new IncorrectUsageException();
    } else {
      EmbedBuilder builder = new EmbedBuilder();
      builder.setFooter("https://github.com/brian-chim/ThisIsAFakeBot");
      MessageChannel channel = input.getChannel();
      User author = input.getAuthor();
      String[] content = input.getContentRaw().split(" ");
      int choice = choice(content[1]);
      if (choice == -1) {
        throw new IncorrectUsageException();
      }
      int numPts;
      try {
        numPts = Integer.parseInt(content[2]);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        throw new IncorrectUsageException();
      }
      if (PointsDBHandler.getPoints(author) < numPts) {
        String invalid = "You don't have enough points to do that.";
        channel.sendMessage(invalid).queue();
        return;
      }
      Random rand = new Random();
      int winnerNum = rand.nextInt(2);
      String winner;
      if (winnerNum == 0) {
        winner = "Heads";
        builder.setImage(
            "https://raw.githubusercontent.com/brian-chim/ThisIsAFakeBot/master/src/resources/points/betcoin/heads.png");
      } else {
        winner = "Tails";
        builder.setImage(
            "https://raw.githubusercontent.com/brian-chim/ThisIsAFakeBot/master/src/resources/points/betcoin/tails.png");
      }
      String msg = "The coin landed " + winner + "! ";
      if (winnerNum == choice) {
        PointsDBHandler.addPoints(author, numPts);
        msg += "You earned " + numPts + " points!";
      } else {
        try {
          PointsDBHandler.removePoints(author, numPts);
        } catch (NegativePointsException e) {
          // shouldn't ever occur because we check for negative points before
          channel.sendMessage("Something went wrong! Try again later.").queue();
          return;
        }
        msg += "You lost " + numPts + " points!";
      }
      msg += " You now have " + PointsDBHandler.getPoints(author) + " points.";
      builder.setDescription(msg);
      channel.sendMessage(builder.build()).queue();
    }
  }

  public int choice(String s) {
    if (s.equalsIgnoreCase("heads") || s.equalsIgnoreCase("h") || s.equalsIgnoreCase("head")) {
      return 0;
    } else if (s.equalsIgnoreCase("tails") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("tail")) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " [heads/h/tails/t] [# of Points]" + "``";
    channel.sendMessage(msg).queue();
  }

  @Override
  public String commandDescription() {
    String ret = "Bets a number points on a coin flip.\n" + "Usage: " + App.botPrefix + commandHandled
        + " [heads/h/tails/t] [# of Points]";
    return ret;
  }

}
