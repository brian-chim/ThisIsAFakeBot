package com.thisisafakecom.thisisafakebot.commands;

import java.util.ArrayList;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.etc.GoodnightCommand;
import com.thisisafakecom.thisisafakebot.commands.etc.RepeatCommand;
import com.thisisafakecom.thisisafakebot.commands.music.CurrCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ListCommand;
import com.thisisafakecom.thisisafakebot.commands.music.LoopCommand;
import com.thisisafakecom.thisisafakebot.commands.music.PlayCommand;
import com.thisisafakecom.thisisafakebot.commands.music.SkipCommand;
import com.thisisafakecom.thisisafakebot.commands.music.StopCommand;
import com.thisisafakecom.thisisafakebot.commands.points.GetPointsCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {

	private ArrayList<ICommand> handledCommands = new ArrayList<ICommand>();

	public CommandHandler() {
		// etc
		handledCommands.add(new RepeatCommand());
		handledCommands.add(new GoodnightCommand());
		// music
		handledCommands.add(new PlayCommand());
		handledCommands.add(new ListCommand());
		handledCommands.add(new SkipCommand());
		handledCommands.add(new CurrCommand());
		handledCommands.add(new StopCommand());
		handledCommands.add(new LoopCommand());
		// points
		handledCommands.add(new GetPointsCommand());
	}

	public void handleCommand(Message input) throws CommandNotSupportedException {
		String[] tokenized = input.getContentRaw().split(" ");
		boolean handled = false;
		for (ICommand command : handledCommands) {
			String cmd = tokenized[0].substring(App.botPrefix.length());
			if (command.getCommandHandled().equalsIgnoreCase(cmd)) {
				try {
					command.handle(input);
				} catch (IncorrectUsageException e) {
					// catch and post the correct usage
					command.correctUsage(input);
				}
				handled = true;
				break;
			}
		}
		if (!handled) {
			throw new CommandNotSupportedException();
		}
	}
}
