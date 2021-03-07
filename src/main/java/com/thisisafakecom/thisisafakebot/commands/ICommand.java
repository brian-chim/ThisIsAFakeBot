package com.thisisafakecom.thisisafakebot.commands;

import net.dv8tion.jda.api.entities.Message;

public interface ICommand {

  public static final String description = "";

  public String getCommandHandled();

  public void handle(Message input) throws IncorrectUsageException;

  public void correctUsage(Message input);

  public String commandDescription();

}
