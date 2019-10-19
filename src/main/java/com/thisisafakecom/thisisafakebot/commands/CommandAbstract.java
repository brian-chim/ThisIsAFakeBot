package com.thisisafakecom.thisisafakebot.commands;

import net.dv8tion.jda.api.entities.Message;

public abstract class CommandAbstract implements ICommand {

  protected String commandHandled;
  protected String[] subCommands;
 
  public String getCommandHandled() {
    return this.commandHandled;
  }

  public abstract void handle(Message input) throws IncorrectUsageException;
  
  public abstract void correctUsage(Message input);
}
