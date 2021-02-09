package com.thisisafakecom.thisisafakebot.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.etc.ChooseCommand;
import com.thisisafakecom.thisisafakebot.commands.etc.GoodnightCommand;
import com.thisisafakecom.thisisafakebot.commands.etc.HelpCommand;
import com.thisisafakecom.thisisafakebot.commands.etc.RepeatCommand;
import com.thisisafakecom.thisisafakebot.commands.music.CurrCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ExportYTPCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ImportYTPCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ListCommand;
import com.thisisafakecom.thisisafakebot.commands.music.LoopCommand;
import com.thisisafakecom.thisisafakebot.commands.music.PlayCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ShuffleCommand;
import com.thisisafakecom.thisisafakebot.commands.music.SkipCommand;
import com.thisisafakecom.thisisafakebot.commands.music.StopCommand;
import com.thisisafakecom.thisisafakebot.commands.points.BetCoinCommand;
import com.thisisafakecom.thisisafakebot.commands.points.GetPointsCommand;
import com.thisisafakecom.thisisafakebot.commands.points.GivePointsCommand;
import com.thisisafakecom.thisisafakebot.commands.points.StartRaceCommand;
import com.thisisafakecom.thisisafakebot.commands.points.trivia.TriviaCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {

	private ArrayList<ArrayList<ICommand>> handledCommands = new ArrayList<ArrayList<ICommand>>();
	private static ArrayList<ICommand> etcCommands = new ArrayList<ICommand>();
	private static ArrayList<ICommand> musicCommands = new ArrayList<ICommand>();
	private static ArrayList<ICommand> pointsCommands = new ArrayList<ICommand>();
	private static final CommandHandler handler = new CommandHandler();

	public CommandHandler() {
		// music
		musicCommands.add(new PlayCommand());
		musicCommands.add(new ListCommand());
		musicCommands.add(new SkipCommand());
		musicCommands.add(new CurrCommand());
		musicCommands.add(new StopCommand());
		musicCommands.add(new ShuffleCommand());
		musicCommands.add(new LoopCommand());
		musicCommands.add(new ExportYTPCommand());
		musicCommands.add(new ImportYTPCommand());
		// points
		pointsCommands.add(new TriviaCommand());
		pointsCommands.add(new GivePointsCommand());
		pointsCommands.add(new GetPointsCommand());
		pointsCommands.add(new BetCoinCommand());
		pointsCommands.add(new StartRaceCommand());
		// etc
		etcCommands.add(new RepeatCommand());
		etcCommands.add(new ChooseCommand());
		etcCommands.add(new GoodnightCommand());
		etcCommands.add(new HelpCommand());
		// overall handler
		handledCommands.add(etcCommands);
		handledCommands.add(musicCommands);
		handledCommands.add(pointsCommands);
	}

	public void handleCommand(Message input) throws CommandNotSupportedException {
		String[] tokenized = input.getContentRaw().split(" ");
		boolean handled = false;
		for (ArrayList<ICommand> commandList: handledCommands) {
			for (ICommand command : commandList) {
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
		}
		if (!handled) {
			throw new CommandNotSupportedException();
		}
	}

	public static CommandHandler getHandler() {
		return handler;
	}
	
	public static LinkedHashMap<String, String> buildHelpDesc() {
		// music
		String music = "";
		for (ICommand command : musicCommands) {
			music += command.getCommandHandled() + ":\n\t";
			music += command.commandDescription().replace("\n", "\n\t") + "\n";
		}
		// points
		String points = "";
		for (ICommand command : pointsCommands) {
			points += command.getCommandHandled() + ":\n\t";
			points += command.commandDescription().replace("\n", "\n\t") + "\n";
		}
		// etc
		String etc = "";
		for (ICommand command : etcCommands) {
			etc += command.getCommandHandled() + ":\n\t";
			etc += command.commandDescription().replace("\n", "\n\t") + "\n";
		}
		LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
		ret.put("Music Commands", music);
		ret.put("Points Commands", points);
		ret.put("Other Commands", etc);
		return ret;
	}
}
