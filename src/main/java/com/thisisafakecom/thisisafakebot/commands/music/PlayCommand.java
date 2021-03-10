package com.thisisafakecom.thisisafakebot.commands.music;

import java.net.URL;
import java.time.Duration;
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
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;

public class PlayCommand extends CommandAbstract {

  private VoiceChannel vc;

  // Credit:
  // https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/Main.java
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
    } catch (Exception e) {
      System.err.println("not a url");
    }
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
          if (title.length() >= 50) {
            title = title.substring(0, 48);
            title += "...";
          }
          Duration dur = Duration.parse(vidList.get(i).videoLength);
          String seconds = String.valueOf(dur.getSeconds() % 60);
          seconds = seconds.length() == 1 ? "0" + seconds : seconds;
          title += "    (" + dur.getSeconds() / 60 + ":" + dur.getSeconds() % 60 + ")";
          msg += "```" + (i + 1) + ":   " + title + "```";
        }
        channel.sendMessage("Make a selection using the corresponding reaction.").queue();
        String unicodes = "";
        for (int i = 1; i < vidList.size() + 1; i++) {
          // https://unicode.org/emoji/charts/full-emoji-list.html#0031_fe0f_20e3
          // keycaps
          String temp = "U+3" + i + "U+fe0fU+20e3";
          unicodes += temp + " ";
        }
        final String u = unicodes;
        channel.sendMessage(msg).queue(choices -> {
          // add the appropriate reactions
          for (int i = 1; i < vidList.size() + 1; i++) {
            // https://unicode.org/emoji/charts/full-emoji-list.html#0031_fe0f_20e3
            // keycaps
            String temp = "U+3" + i + "U+fe0fU+20e3";
            choices.addReaction(temp).queue();
          }
          long callbackMsgId = choices.getIdLong();
          App.waiter
              .waitForEvent(GenericGuildMessageReactionEvent.class,
                  e -> e.getUser().equals(input.getAuthor()) && e.getReaction().getChannel().equals(input.getChannel())
                      && u.contains(e.getReactionEmote().getAsCodepoints()) && e.getMessageIdLong() == callbackMsgId,
                  e -> {
                    int num = Integer.parseInt(e.getReactionEmote().getAsCodepoints().substring(3, 4));
                    loadAndPlay(textChannel, "https://www.youtube.com/watch?v=" + vidList.get(num - 1).videoId);
                    return;
                  }, 20, TimeUnit.SECONDS, () -> channel.sendMessage("No selection picked in time!").queue());
        });
        return;
      } catch (Exception e) {
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
        for (AudioTrack track : playlist.getTracks()) {
          play(channel.getGuild(), musicManager, track);
        }
        channel.sendMessage("Added " + playlist.getTracks().size() + " songs to queue from " + playlist.getName() + ".")
            .queue();
      }

      @Override
      public void noMatches() {
        channel.sendMessage("Nothing found by " + trackUrl).queue();
      }

      @Override
      public void loadFailed(FriendlyException exception) {
        exception.printStackTrace();
        channel.sendMessage("Could not play: " + exception.getMessage()).queue();
      }
    });
  }

  private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
    guild.getAudioManager().openAudioConnection(vc);
    musicManager.scheduler.queue(track);
  }

  @Override
  public String commandDescription() {
    String ret = "Plays a song from a youtube URL or searches for one off given keywords.\n" + "Usage: " + App.botPrefix
        + commandHandled + " [youtube URL OR search terms]";
    return ret;
  }
}
