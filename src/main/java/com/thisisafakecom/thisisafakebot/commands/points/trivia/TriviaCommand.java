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
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

public class TriviaCommand extends CommandAbstract {

	Map<User, Integer> winners;
	int delayValue = 20;
	int currDelay;
	Set<User> answeredOnce;
	Set<User> answeredMore;
	Set<User> answeredCorrect;
	
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
			// make a call to get a trivia question
			TriviaHandler th = TriviaHandler.getInstance();
			GuildTriviaManager triviaManager = th.getGuildTriviaManager(channel.getGuild());
			ArrayList<TriviaQuestion> questions = triviaManager.getVideogameQuestions();
			winners = new HashMap<User, Integer>();
			for (int i = 1; i <= questions.size(); i++) {
				TriviaQuestion curr = questions.get(i - 1);
				ArrayList<String> possibleAnswers = curr.incorrect_answers;
				possibleAnswers.add(curr.correct_answer);
				Collections.shuffle(possibleAnswers);
				possibleAnswers.replaceAll(StringEscapeUtils::unescapeHtml4);
				curr.question = StringEscapeUtils.unescapeHtml4(curr.question);
				curr.correct_answer = StringEscapeUtils.unescapeHtml4(curr.correct_answer);
				String msg = "Question " + i + " of " + questions.size() + ": " + curr.question + "\n";
				for (int j = 1; j < possibleAnswers.size() + 1; j++) {
					msg += j + ": " + possibleAnswers.get(j - 1) + "\n";
				}
				final String msgBase = msg;
				// if first message then send right away else queue it to send in 30 seconds
				currDelay = (i - 1) * delayValue;
				channel.sendMessage(msg).queueAfter(currDelay, TimeUnit.SECONDS,
						sent -> {
							for (int k = 1; k < possibleAnswers.size() + 1; k++) {
								String temp = "U+3" + k + "U+fe0fU+20e3";
								sent.addReaction(temp).queue();
							}
							int corrAnswer = possibleAnswers.indexOf(curr.correct_answer) + 1;
//							System.out.println("Answer is " + corrAnswer);
							String msgAnswer = "The correct answer was " + corrAnswer + ": " + possibleAnswers.get(corrAnswer - 1);
							final String msgFinal = msgBase + msgAnswer;
							sent.editMessage(msgFinal).queueAfter(delayValue, TimeUnit.SECONDS);
							long callbackMsgId = sent.getIdLong();
							App.waiter.waitForEvent(GuildMessageUpdateEvent.class,
									update -> update.getAuthor().equals(sent.getAuthor())
									&& update.getMessage().getContentRaw().contains(msgFinal)
									&& update.getMessageIdLong() == callbackMsgId,
									update -> {
										// this doesnt return me the current reactions because message object wasnt updated?
										//	List<MessageReaction> reactions = update.getMessage().getReactions();
										// instead do it by callback
										channel.retrieveMessageById(update.getMessageIdLong()).queue(
												(retrievedMsg) -> {
													List<MessageReaction> reactions = retrievedMsg.getReactions();
													// enforce that each user can only answer once
													answeredOnce = new HashSet<User>();
													answeredMore = new HashSet<User>();
													answeredCorrect = new HashSet<User>();
													for (MessageReaction reaction : reactions) {
														reaction.retrieveUsers().forEachAsync((user) -> {
															if (reaction.getReactionEmote().getAsCodepoints().substring(3, 4).equals(Integer.toString(corrAnswer))) {
																answeredCorrect.add(user);
																updateWinners(user);
															}
//															if (answeredOne.contains(user)) {
//																answeredMore.add(user);
//															} else {
//																answeredOne.add(user);
//																if (reaction.getReactionEmote().getAsCodepoints().substring(3, 4).equals(Integer.toString(corrAnswer))) {
//																	if (!user.isBot()) {
//																		answeredCorrect.add(user);
//																	}
//																}
//															}
															return true;
														});
													}
													answeredCorrect.removeAll(answeredMore);
													answeredCorrect.forEach((user) -> updateWinners(user));
												}
											);
										},
									// spacing it out a bit from the edit time
									delayValue + 5, TimeUnit.SECONDS, () -> System.err.print("Something went wrong in trivia. Failed to wait for the message update."));
						});
			}
			String exitMsg = "Trivia Complete!";//\nPoints Gained:\n";
//			for (User user : winners.keySet()) {
//				Integer pts = winners.get(user);
//				exitMsg += user.getName() + ": " + pts + " Points\n";
//				//PointsDBHandler.addPoints(user, pts.intValue());
//			}
			channel.sendMessage(exitMsg).queueAfter(currDelay + delayValue, TimeUnit.SECONDS);
		}
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
		String ret = "Starts a game of Trivia. You get one point per correct question answered.\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}
}
