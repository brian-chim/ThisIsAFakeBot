package com.thisisafakecom.thisisafakebot;

import javax.security.auth.login.LoginException;

import com.thisisafakecom.thisisafakebot.commands.CommandHandler;
import com.thisisafakecom.thisisafakebot.commands.CommandNotSupportedException;
import com.thisisafakecom.thisisafakebot.commands.points.PointsHandler;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class App extends ListenerAdapter 
{

  private final String botPrefix = "!";
  private final CommandHandler ch = new CommandHandler();

  public static void main( String[] args ) throws LoginException {
    JDABuilder bot = new JDABuilder(AccountType.BOT);
    bot.setToken("token");
    bot.addEventListener(new App());
    bot.build();
    //TODO add all missing users to the db on start up
  }

  @Override
  public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    if(!event.getAuthor().isBot()) {
      Message msgReceived = event.getMessage();
      if (event.getMessage().getContentRaw().startsWith(botPrefix)) {
        try {
          ch.handleCommand(msgReceived);
        } catch (CommandNotSupportedException e) {
          event.getChannel().sendMessage("Command not found!").queue();
        }
      }
      PointsHandler.autoGenPoints(msgReceived);
    }
  }
}
