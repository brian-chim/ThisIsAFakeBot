package com.thisisafakecom.thisisafakebot.commands;

import java.util.ArrayList;

import com.thisisafakecom.thisisafakebot.commands.etc.GoodnightCommand;
import com.thisisafakecom.thisisafakebot.commands.etc.RepeatCommand;
import com.thisisafakecom.thisisafakebot.commands.music.ListCommand;
import com.thisisafakecom.thisisafakebot.commands.music.PlayCommand;
import com.thisisafakecom.thisisafakebot.commands.points.GetPointsCommand;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {

  private ArrayList<ICommand> handledCommands = new ArrayList<ICommand>();
  
  public CommandHandler() {
    handledCommands.add(new RepeatCommand());
    handledCommands.add(new GetPointsCommand());
    handledCommands.add(new PlayCommand());
    handledCommands.add(new ListCommand());
    handledCommands.add(new GoodnightCommand());
  }

  public void handleCommand(Message input) throws CommandNotSupportedException {
    String[] tokenized = input.getContentRaw().split(" ");
    boolean handled = false;
    for (ICommand command : handledCommands) {
      String cmd = tokenized[0].substring(1); 
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
    if(!handled) {
      throw new CommandNotSupportedException();
    }
  }
}
