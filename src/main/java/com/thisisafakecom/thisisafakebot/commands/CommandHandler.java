package com.thisisafakecom.thisisafakebot.commands;

import java.util.ArrayList;

import com.thisisafakecom.thisisafakebot.commands.etc.RepeatCommand;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {

  private ArrayList<ICommand> handledCommands = new ArrayList<ICommand>();
  
  public CommandHandler() {
    handledCommands.add(new RepeatCommand());
  }

  public void handleCommand(Message input) throws CommandNotSupportedException {
    String[] tokenized = input.getContentRaw().split(" ");
    boolean handled = false;
    for (ICommand command : handledCommands) {
      String cmd = tokenized[0].substring(1); 
      if (command.getCommandHandled().equals(cmd)) {
        try {
          command.handle(input);
        } catch (IncorrectUsageException e) {
          // catch and post the correct usage
          command.correctUsage(input);
        }
        handled = true;
      }
    }
    if(!handled) {
      throw new CommandNotSupportedException();
    }
  }
}
