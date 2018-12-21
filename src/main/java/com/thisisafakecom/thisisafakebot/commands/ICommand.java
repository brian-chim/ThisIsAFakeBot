package com.thisisafakecom.thisisafakebot.commands;

import net.dv8tion.jda.core.entities.Message;

public interface ICommand {

  public String getCommandHandled();
  
  public void handle(Message input) throws IncorrectUsageException;

  public void correctUsage(Message input);
}
