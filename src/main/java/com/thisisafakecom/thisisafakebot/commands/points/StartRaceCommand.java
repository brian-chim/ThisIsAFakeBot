package com.thisisafakecom.thisisafakebot.commands.points;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

public class StartRaceCommand extends CommandAbstract {

  long msgId;
  int raceLength = 20;
  double lowerBound = 0.6;
  int countdown = 20;

  public StartRaceCommand() {
    commandHandled = "startrace";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1) {
      throw new IncorrectUsageException();
    } else {
      // to achieve proper timing of message sending, simulate the horse race first
      // and then
      // display it in the chat and ask the users to guess the winner
      String horseRace = "";
      for (int k = 0; k < 4; k++) {
        for (int j = 0; j < raceLength; j++) {
          if (j == raceLength - 1) {
            horseRace += ":horse_racing:";
          } else {
            horseRace += "-";
          }
        }
        horseRace += "END";
      }
      String[] horseTracks = horseRace.split("END");
      // once race has started, keep editing until a horse has won
      boolean winner = false;
      int horseAPos = raceLength - 1;
      int horseBPos = raceLength - 1;
      int horseCPos = raceLength - 1;
      int horseDPos = raceLength - 1;
      Random rng = new Random();
      List<String[]> state = new ArrayList<String[]>();
      // init state
      state.add(horseTracks.clone());
      List<Integer> winners = new ArrayList<Integer>();
      while (!winner) {
        // horse 1
        if (rng.nextDouble() < lowerBound) {
          horseAPos -= 1;
          horseTracks[0] = horseTracks[0].replace("-:horse_racing:", ":horse_racing:-");
        }
        // horse 2
        if (rng.nextDouble() < lowerBound) {
          horseBPos -= 1;
          horseTracks[1] = horseTracks[1].replace("-:horse_racing:", ":horse_racing:-");
        }
        // horse 3
        if (rng.nextDouble() < lowerBound) {
          horseCPos -= 1;
          horseTracks[2] = horseTracks[2].replace("-:horse_racing:", ":horse_racing:-");
        }
        // horse 4
        if (rng.nextDouble() < lowerBound) {
          horseDPos -= 1;
          horseTracks[3] = horseTracks[3].replace("-:horse_racing:", ":horse_racing:-");
        }
        state.add(horseTracks.clone());
        // horse won!
        if (horseAPos == 0) {
          winner = true;
          winners.add(1);
        }
        if (horseBPos == 0) {
          winner = true;
          winners.add(2);
        }
        if (horseCPos == 0) {
          winner = true;
          winners.add(3);
        }
        if (horseDPos == 0) {
          winner = true;
          winners.add(4);
        }
      }
      MessageChannel channel = input.getChannel();
      // countdown a race start
      Message msg = channel.sendMessage(
          "The race will begin in " + countdown + " seconds.\n" + "Please choose a horse 1-4 that you think will win!\n"
              + state.get(0)[0] + "\n" + state.get(0)[1] + "\n" + state.get(0)[2] + "\n" + state.get(0)[3] + "\n")
          .complete();
      // add reactions to select the horse
      msgId = msg.getIdLong();
      List<String> winnerUnicodes = new ArrayList<String>();
      for (int k = 1; k <= 4; k++) {
        String temp = "U+3" + k + "U+fe0fU+20e3";
        if (winners.contains(k)) {
          winnerUnicodes.add(temp);
        }
        msg.addReaction(temp).queue();
      }
      // :horse_racing:
      for (int i = countdown - 1; i > 0; i--) {
        channel
            .editMessageById(msgId,
                "The race will begin in " + i + " seconds.\n" + "Please choose a horse 1-4 that you think will win!\n"
                    + state.get(0)[0] + "\n" + state.get(0)[1] + "\n" + state.get(0)[2] + "\n" + state.get(0)[3] + "\n")
            .queueAfter(countdown - i, TimeUnit.SECONDS);
      }
      // lock in the votes and get the winners
      Set<User> winningUsers = new HashSet<User>();
      Set<User> otherChoice = new HashSet<User>();
      App.waiter.waitForEvent(GuildMessageUpdateEvent.class,
          e -> e.getMessage().getContentRaw().contains("The race will begin in 1 seconds.")
              && e.getMessage().getIdLong() == msgId,
          e -> {
            List<MessageReaction> reactions = channel.retrieveMessageById(msgId).complete().getReactions();
            for (MessageReaction r : reactions) {
              if (winnerUnicodes.contains(r.getReactionEmote().getAsCodepoints())) {
                for (User u : r.retrieveUsers().complete()) {
                  if (winningUsers.contains(u)) {
                    otherChoice.add(u);
                  } else {
                    winningUsers.add(u);
                  }
                }
              } else {
                for (User u : r.retrieveUsers().complete()) {
                  otherChoice.add(u);
                }
              }
            }
            // give the winners their points now
            winningUsers.removeAll(otherChoice);
            for (User u : winningUsers) {
              PointsDBHandler.addPoints(u, 1);
            }
          });
      // display the above simulation of the horse race
      for (int i = 0; i < state.size(); i++) {
        String[] currTrack = state.get(i);
        channel
            .editMessageById(msgId, "Votes have been locked in! The horses are racing:\n" + currTrack[0] + "\n"
                + currTrack[1] + "\n" + currTrack[2] + "\n" + currTrack[3] + "\n")
            .queueAfter(countdown + i, TimeUnit.SECONDS);
      }
      String winnerMsg = "The winner(s) are: ";
      for (int i = 0; i < winners.size(); i++) {
        winnerMsg += "Horse " + winners.get(i) + ", ";
      }
      winnerMsg = winnerMsg.substring(0, winnerMsg.length() - 2) + "!";
      channel
          .editMessageById(msgId,
              winnerMsg + "\n" + state.get(state.size() - 1)[0] + "\n" + state.get(state.size() - 1)[1] + "\n"
                  + state.get(state.size() - 1)[2] + "\n" + state.get(state.size() - 1)[3] + "\n")
          .queueAfter(countdown + state.size(), TimeUnit.SECONDS);
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
    String ret = "Starts a horse race in 20s. Use the emotes to choose your horse!\n" + "Usage: " + App.botPrefix
        + commandHandled;
    return ret;
  }

}
