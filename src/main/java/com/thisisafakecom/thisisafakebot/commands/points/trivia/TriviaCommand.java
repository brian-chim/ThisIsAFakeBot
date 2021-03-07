package com.thisisafakecom.thisisafakebot.commands.points.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringEscapeUtils;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.points.database.PointsDBHandler;
import com.thisisafakecom.thisisafakebot.commands.points.trivia.handlers.GuildTriviaManager;
import com.thisisafakecom.thisisafakebot.commands.points.trivia.handlers.TriviaHandler;
import com.thisisafakecom.thisisafakebot.commands.points.trivia.handlers.TriviaQuestion;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

public class TriviaCommand extends CommandAbstract {

  Map<User, Integer> winners;
  int delayValue = 20;
  int currDelay;
  Set<User> answeredWrong;
  Set<User> answeredCorrect;
  long completeId = -1;
  String completeMsg = "Trivia Complete!";

  public TriviaCommand() {
    commandHandled = "trivia";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1) {
      throw new IncorrectUsageException();
    } else {
      // -trivia
      TextChannel channel = input.getTextChannel();
      Message start = channel.sendMessage("Trivia is starting!").complete();
      completeId = -1;
      // make a call to get a trivia question
      TriviaHandler th = TriviaHandler.getInstance();
      GuildTriviaManager triviaManager = th.getGuildTriviaManager(channel.getGuild());
      ArrayList<TriviaQuestion> questions = triviaManager.getVideogameQuestions();
      winners = new HashMap<User, Integer>();
      for (int i = 1; i <= questions.size(); i++) {
        currDelay = (i - 1) * delayValue;
        oneInstance(questions.get(i - 1), channel, i, questions.size(), start.getAuthor());
      }
      App.waiter.waitForEvent(
          GuildMessageReceivedEvent.class, exit -> exit.getAuthor() == start.getAuthor()
              && exit.getMessage().getContentRaw().contains(completeMsg) && exit.getMessage().getIdLong() == completeId,
          exit -> {
            String exitMsg = "Points Gained:\n";
            for (User user : winners.keySet()) {
              Integer pts = winners.get(user);
              exitMsg += user.getName() + ": " + pts + " Points\n";
              PointsDBHandler.addPoints(user, pts.intValue());
            }
            channel.sendMessage(exitMsg).queue();
          });
    }
  }

  public void oneInstance(TriviaQuestion tq, TextChannel channel, int currNum, int totalNum, User bot) {
    tq.question = StringEscapeUtils.unescapeHtml4(tq.question);
    tq.correct_answer = StringEscapeUtils.unescapeHtml4(tq.correct_answer);
    tq.incorrect_answers.replaceAll(StringEscapeUtils::unescapeHtml4);
    ArrayList<String> possibleAnswers = tq.incorrect_answers;
    possibleAnswers.add(tq.correct_answer);
    Collections.shuffle(possibleAnswers);
    String msg = "Question " + currNum + " of " + totalNum + ": " + tq.question + "\n";
    for (int j = 1; j < possibleAnswers.size() + 1; j++) {
      msg += j + ": " + possibleAnswers.get(j - 1) + "\n";
    }
    int corrAnswer = possibleAnswers.indexOf(tq.correct_answer) + 1;
    String msgAnswer = "The correct answer was " + corrAnswer + ": " + possibleAnswers.get(corrAnswer - 1);
    final String msgFinal = msg + msgAnswer;
    // send message with question
    channel.sendMessage(msg).queueAfter(currDelay, TimeUnit.SECONDS, (callback) -> {
      // add the reactions in the callback
      for (int k = 1; k < possibleAnswers.size() + 1; k++) {
        String temp = "U+3" + k + "U+fe0fU+20e3";
        callback.addReaction(temp).queue();
      }
      // prep the edit
      callback.editMessage(msgFinal).queueAfter(delayValue, TimeUnit.SECONDS);
    });
    // tell it to wait for the edit before locking in and adding points
    App.waiter.waitForEvent(GuildMessageUpdateEvent.class,
        update -> update.getAuthor().equals(bot) && update.getMessage().getContentRaw().contains(msgFinal), update -> {
          Message updateMsg = channel.retrieveMessageById(update.getMessageIdLong()).complete();
          List<MessageReaction> reactions = updateMsg.getReactions();
          answeredWrong = new HashSet<User>();
          answeredCorrect = new HashSet<User>();
          for (MessageReaction r : reactions) {
            for (User u : r.retrieveUsers()) {
              if (r.getReactionEmote().getAsCodepoints().substring(3, 4).equals(Integer.toString(corrAnswer))) {
                answeredCorrect.add(u);
              } else {
                answeredWrong.add(u);
              }
            }
          }
          answeredCorrect.removeAll(answeredWrong);
          answeredCorrect.forEach((user) -> updateWinners(user));
          // adding a case for final question to signal the end of trivia
          if (currNum == totalNum) {
            Message complete = channel.sendMessage(completeMsg).complete();
            completeId = complete.getIdLong();
          }
          // offsetting the delay value by a little bit to avoid race condition
        }, currDelay + (delayValue * 2), TimeUnit.SECONDS,
        () -> System.err.println("Something went wrong in trivia. Didnt catch the question update"));
  }

  public Map<User, Integer> updateWinners(User user) {
    Integer num = winners.get(user);
    if (num == null) {
      winners.put(user, 1);
    } else {
      winners.put(user, num + 1);
    }
    return winners;
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
    channel.sendMessage(msg).queue();
  }

  @Override
  public String commandDescription() {
    String ret = "Starts a game of Trivia. You get one point per correct question answered.\n" + "Usage: "
        + App.botPrefix + commandHandled;
    return ret;
  }
}
