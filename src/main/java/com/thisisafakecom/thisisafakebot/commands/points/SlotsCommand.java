package com.thisisafakecom.thisisafakebot.commands.points;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class SlotsCommand extends CommandAbstract {

  LinkedList<String> iconOrder = new LinkedList<String>();
  String iconA = ":star:";
  String iconB = ":heart:";
  String iconC = ":brain:";
  String iconD = ":moneybag:";
  int numIcon;
  int minSpin = 10;
  int maxAddSpin = 10;
  double bound = 0.9;

  public SlotsCommand() {
    commandHandled = "slots";
    iconOrder.add(iconA);
    iconOrder.add(iconB);
    iconOrder.add(iconC);
    iconOrder.add(iconD);
    numIcon = iconOrder.size();
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1) {
      throw new IncorrectUsageException();
    } else {
      MessageChannel channel = input.getChannel();
      User user = input.getAuthor();
      Random r = new Random();
      int colOneInd = r.nextInt(numIcon);
      int colTwoInd = r.nextInt(numIcon);
      int colThrInd = r.nextInt(numIcon);
      int spinsOne = minSpin + r.nextInt(maxAddSpin);
      int spinsTwo = minSpin + r.nextInt(maxAddSpin);
      int spinsThr = minSpin + r.nextInt(maxAddSpin);
      int maxSpins = Math.max(spinsOne, Math.max(spinsTwo, spinsThr));
      List<String> state = new ArrayList<String>();
      for (int i = 0; i < maxSpins; i++) {
        if (i < spinsOne && r.nextDouble() < bound) { colOneInd = getNextIndex(colOneInd); }
        if (i < spinsTwo && r.nextDouble() < bound) { colTwoInd = getNextIndex(colTwoInd); }
        if (i < spinsThr && r.nextDouble() < bound) { colThrInd = getNextIndex(colThrInd); }
        state.add(iconOrder.get(colOneInd) + " " + iconOrder.get(colTwoInd) + " " + iconOrder.get(colThrInd));
      }
      Message msg = channel.sendMessage("Slots is starting: ").complete();
      for (int j = 0; j < state.size(); j++) {
        String[] curr = state.get(j).split(" ");
        String prev = iconOrder.get(getPrevIndex((iconOrder.indexOf(curr[0])))) + " " + 
                      iconOrder.get(getPrevIndex((iconOrder.indexOf(curr[1])))) + " " + 
                      iconOrder.get(getPrevIndex((iconOrder.indexOf(curr[2]))));
        String afte = iconOrder.get(getNextIndex((iconOrder.indexOf(curr[0])))) + " " + 
                      iconOrder.get(getNextIndex((iconOrder.indexOf(curr[1])))) + " " + 
                      iconOrder.get(getNextIndex((iconOrder.indexOf(curr[2]))));
        String slotMsg = "F A K E S L O T S\n| " + prev + " |\n| " + state.get(j) + " | <-- \n| " + afte + " |";
        channel.editMessageById(msg.getIdLong(), slotMsg).queueAfter(j + 1, TimeUnit.SECONDS);
        if (j == state.size() - 1) {
          if (curr[0].equalsIgnoreCase(curr[1]) && curr[1].equalsIgnoreCase(curr[2])) {
            PointsDBHandler.addPoints(user, 5);
            channel.editMessageById(msg.getIdLong(), slotMsg + "\nCongratulations! You won 5 points!").queueAfter(j + 3, TimeUnit.SECONDS);
          } else {
            channel.editMessageById(msg.getIdLong(), slotMsg + "\nOh no! You didn't win anything :(").queueAfter(j + 3, TimeUnit.SECONDS);
          }
        }
      }
    }
  }

  public int getNextIndex(int currIndex) {
    return (currIndex == iconOrder.size() - 1 ? 0 : currIndex + 1);
  }

  public int getPrevIndex(int currIndex) {
    return (currIndex == 0 ? iconOrder.size() - 1 : currIndex - 1);
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
    channel.sendMessage(msg).queue();
  }

  @Override
  public String commandDescription() {
    String ret = "Runs the Fake Slots. You could win 5 points!\n" + "Usage: " + App.botPrefix + commandHandled;
    return ret;
  }

}
