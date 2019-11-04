package com.thisisafakecom.thisisafakebot.commands.etc;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class GoodnightCommand extends CommandAbstract {

	public GoodnightCommand() {
		commandHandled = "goodnight";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		String[] tokenized = input.getContentRaw().split(" ");
		if (tokenized.length != 1) {
			throw new IncorrectUsageException();
		} else {
			String msg = "Goodnight " + input.getAuthor().getAsMention();
			MessageChannel channel = input.getChannel();
			channel.sendMessage(msg).queue();
		}
	}

	@Override
	public void correctUsage(Message input) {
		MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
		channel.sendMessage(msg).queue();
	}

}
