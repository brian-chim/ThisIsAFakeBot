package com.thisisafakecom.thisisafakebot.commands.music;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.YoutubeHandler;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.YoutubeSearchInfo;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlayCommand extends CommandAbstract {

	private VoiceChannel vc;

	// Credit: https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/Main.java
	public PlayCommand() {
		commandHandled = "play";
	}

	@Override
	public void handle(Message input) throws IncorrectUsageException {
		MessageChannel channel = input.getChannel();
		GuildVoiceState vs = input.getMember().getVoiceState();
		boolean properUsage = false;
		if (vs != null) {
			if (vs.getChannel() != null) {
				properUsage = true;
			}
		}
		if (!properUsage) {
			String msg = "You must be in a voice channel to use that!";
			channel.sendMessage(msg).queue();
			throw new IncorrectUsageException();
		}
		this.vc = vs.getChannel();
	    TextChannel textChannel = input.getTextChannel();
		// +1 for the space
		String urlOrSearch = input.getContentRaw().substring(App.botPrefix.length() + commandHandled.length() + 1);
		boolean isUrl = false;
		try {
			URL testUrl = new URL(urlOrSearch);
			testUrl.toURI();
			isUrl = true;
		} catch (Exception e) {System.err.println("not a url");}
		if (isUrl) {
			loadAndPlay(textChannel, urlOrSearch);
			return;
		} else {
			// get the list of videos for the query
			try {
				ArrayList<YoutubeSearchInfo> vidList = YoutubeHandler.searchVideos(urlOrSearch);
				String msg = "";
				for (int i = 0; i < vidList.size(); i++) {
					String title = vidList.get(i).videoTitle;
					if (title.length() >= 60) {
						title = title.substring(0, 58);
						title += "...";
					} else {
						while (title.length() < 60) {
							title += " ";
						}	
					}
					msg += "```" + (i+1) + ":   " + title + "```";
				}
				// TODO make a selection using the number emojis?
				channel.sendMessage("Make a selection using 1-" + vidList.size()).queue();
				channel.sendMessage(msg).queue();
				App.waiter.waitForEvent(MessageReceivedEvent.class,
						e -> e.getAuthor().equals(input.getAuthor())
						&& e.getChannel().equals(input.getChannel())
						&& !e.getMessage().equals(input),
						e -> {
							int num = isValidSelection(e.getMessage(), vidList.size());
							if (num > 0) {
								loadAndPlay(textChannel, "https://www.youtube.com/watch?v=" + 
										vidList.get(num - 1).videoId);
							} else {
								if (!e.getMessage().getContentRaw().startsWith(App.botPrefix)) {
									channel.sendMessage("Not a valid selection! Please start your search again.").queue();
								}
								return;
							}},
						30, TimeUnit.SECONDS, () -> channel.sendMessage("No selection picked in time!").queue());
				return;
			} catch (Exception e) {
				// TODO failing possibly because search result does not contain given value ex. videoId if not video
				// maybe a movie?
				e.printStackTrace();
				channel.sendMessage("Failed to search.").queue();
				return;
			}
		}
	}

	@Override
	public void correctUsage(Message input) {
	    MessageChannel channel = input.getChannel();
	    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " <YouTube url>``";
	    channel.sendMessage(msg).queue();
	}
	
	private void loadAndPlay(final TextChannel channel, final String trackUrl) {
		MusicHandler mh = MusicHandler.getInstance();
		GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
		
		mh.getAudioPlayerManager().loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
	        	play(channel.getGuild(), musicManager, track);
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();
		        if (firstTrack == null) {
		          firstTrack = playlist.getTracks().get(0);
		        }
		        channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
		        play(channel.getGuild(), musicManager, firstTrack);
		      }
			@Override
			public void noMatches() {
				channel.sendMessage("Nothing found by " + trackUrl).queue();
			}
			@Override
		    public void loadFailed(FriendlyException exception) {
		    	channel.sendMessage("Could not play: " + exception.getMessage()).queue();
		    }
	    });
	}

	private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
		guild.getAudioManager().openAudioConnection(vc);
		musicManager.scheduler.queue(track);
	}

	private int isValidSelection(Message input, int max) {
		try {
			int toTest = Integer.parseInt(input.getContentRaw());
			if (toTest >= 1 && toTest <= max) {
				return toTest;
			}
		} catch (Exception e) {e.printStackTrace();}
		return -1;
	}
}
