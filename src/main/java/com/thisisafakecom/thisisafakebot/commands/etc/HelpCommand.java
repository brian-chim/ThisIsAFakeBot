package com.thisisafakecom.thisisafakebot.commands.etc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.CommandHandler;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;

public class HelpCommand extends CommandAbstract {

	int currentPage = 1;
	boolean active = false;
	int curr = 0;
	String leftArrowUnicode = "U+2b05";
	String rightArrowUnicode = "U+27a1";
	String arrowUnicode = leftArrowUnicode + rightArrowUnicode;
	
	public HelpCommand() {
		commandHandled = "help";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		} else {
			MessageChannel channel = input.getChannel();
			LinkedHashMap<String, String> helpDescs = CommandHandler.buildHelpDesc();
			List<String> categories = new ArrayList<String> (helpDescs.keySet());
			List<String> desc = new ArrayList<String> (helpDescs.values());
			curr = 0;
			String msg = "```Page " + (curr + 1) + " of " + categories.size() + "\n"
					+ categories.get(curr) + "\n" + desc.get(curr) + "```";
			channel.sendMessage(msg).queue(
					msgCallback -> {
						msgCallback.addReaction(leftArrowUnicode).queue();
						msgCallback.addReaction(rightArrowUnicode).queue();
						long id = msgCallback.getIdLong();
						waitForUser(input, id, msgCallback, categories, desc);
					});
			return;
		}
	}

	public void waitForUser(Message input, long reactMsgId, Message msgCallback, List<String> categories, List<String> desc) {
		App.waiter.waitForEvent(GenericGuildMessageReactionEvent.class,
				e -> e.getUser().equals(input.getAuthor())
				&& e.getReaction().getChannel().equals(input.getChannel())
				&& arrowUnicode.contains(e.getReactionEmote().getAsCodepoints())
				&& e.getMessageIdLong() == reactMsgId,
				e -> {
						if (e.getReactionEmote().getAsCodepoints().equals(rightArrowUnicode)) {
							// increment
							curr = curr == categories.size() - 1 ? 0 : curr + 1;
						} else {
							// decrement
							curr = curr == 0 ? categories.size() - 1 : curr - 1;
						}
						msgCallback.clearReactions().queue();
						String edit = "```Page " + (curr + 1) + " of " + categories.size() + "\n"
									+ categories.get(curr) + "\n" + desc.get(curr) + "```";
						msgCallback.editMessage(edit).queue();
						msgCallback.addReaction(leftArrowUnicode).queue();
						msgCallback.addReaction(rightArrowUnicode).queue();
						waitForUser(input, reactMsgId, msgCallback, categories, desc);
					},
				30, TimeUnit.SECONDS, () -> {});
	}

	@Override
	public void correctUsage(Message input) {
		MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
		channel.sendMessage(msg).queue();
	}

	@Override
	public String commandDescription() {
		String ret = "Shows this message to you :)\n"
				+ "Usage: " + App.botPrefix + commandHandled;
		return ret;
	}

}
