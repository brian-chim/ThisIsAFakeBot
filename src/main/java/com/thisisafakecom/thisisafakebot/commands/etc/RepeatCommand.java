package com.thisisafakecom.thisisafakebot.commands.etc;

import org.apache.commons.lang3.StringUtils;

import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Message;

public class RepeatCommand extends CommandAbstract {

  public RepeatCommand() {
    commandHandled = "repeat";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    // split the msg by space
    String[] tokenized = input.getContentRaw().split(" ");
    if(tokenized.length < 3 || !StringUtils.isNumeric(tokenized[tokenized.length - 1])) {
      throw new IncorrectUsageException();
    } else {
      String msg = "";
      // reconstruct the msg
      for(int i = 1; i < tokenized.length - 1; i++) {
        msg += tokenized[i] + " ";
      }
      MessageChannel channel = input.getChannel();
      // send the message the amount of times
      for(int j = 0; j < Integer.parseInt(tokenized[tokenized.length - 1]); j++) {
        channel.sendMessage(msg).queue();        
      }
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``!" + commandHandled + " [your message here] " + "[number of repetitions]``";
    channel.sendMessage(msg).queue();
  }
}
