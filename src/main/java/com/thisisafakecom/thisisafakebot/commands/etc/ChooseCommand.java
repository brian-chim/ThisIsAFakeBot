package com.thisisafakecom.thisisafakebot.commands.etc;

import java.util.Random;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ChooseCommand extends CommandAbstract {

  public ChooseCommand() {
    commandHandled = "choose";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    String[] tokenized = input.getContentRaw().split(" ", 2);
    if (tokenized.length <= 1) {
      throw new IncorrectUsageException();
    } else {
      String[] options = tokenized[1].split(",");
      int selection;
      if (options.length == 1) {
        selection = 0;
      } else {
        Random r = new Random();
        selection = r.nextInt(options.length);
      }
      String choice = options[selection].trim().replaceAll("\\s+", " ");
      String msg = input.getAuthor().getAsMention() + ": I choose " + choice + ".";
      MessageChannel channel = input.getChannel();
      channel.sendMessage(msg).queue();
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " [Option 1],[Option 2],...,[Option n]\n"
        + "Each option must be separated by a comma ," + "``";
    channel.sendMessage(msg).queue();
  }

  public String commandDescription() {
    String ret = "Chooses something between your options.\n" + "Usage: " + App.botPrefix + commandHandled
        + " [Option 1],[Option 2],...,[Option n]";
    return ret;
  }

}
