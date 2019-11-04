package com.thisisafakecom.thisisafakebot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.thisisafakecom.thisisafakebot.commands.CommandHandler;
import com.thisisafakecom.thisisafakebot.commands.CommandNotSupportedException;
import com.thisisafakecom.thisisafakebot.commands.points.PointsHandler;
import com.thisisafakecom.thisisafakebot.database.DBHandler;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class App extends ListenerAdapter {

	public static final String botPrefix = ".";
	private final CommandHandler ch = new CommandHandler();
	public static EventWaiter waiter = new EventWaiter();
	
	public static void main(String[] args) throws LoginException {
		String token = "";
		try {
			token = new String(Files.readAllBytes(Paths.get("token.secret")));
		} catch (IOException e) {
			System.out.println("Failed to retrieve token.");
			return;
		}
		JDABuilder bot = new JDABuilder(AccountType.BOT);
		bot.setToken(token);
		bot.addEventListeners(new App(), waiter);
		bot.build();
		// TODO add all missing users to the db on start up
		DBHandler.initDb();
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
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
