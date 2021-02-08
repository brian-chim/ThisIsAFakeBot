package com.thisisafakecom.thisisafakebot.commands.points.trivia.handlers;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;

public class TriviaHandler {
	private static TriviaHandler instance = null;
	private Map<Long, GuildTriviaManager> triviaManagers;

	private TriviaHandler() {
		triviaManagers = new HashMap<>();
	}

	public synchronized GuildTriviaManager getGuildTriviaManager(Guild guild) {
		long guildId = Long.parseLong(guild.getId());
	    GuildTriviaManager triviaManager = triviaManagers.get(guildId);
	    if (triviaManager == null) {
	    	triviaManager = new GuildTriviaManager();
	    	triviaManagers.put(guildId, triviaManager);
		}
	    return triviaManager;
	}

	public static TriviaHandler getInstance() {
		if (instance == null) {
			instance = new TriviaHandler();
		}
		return instance;
	}
}
