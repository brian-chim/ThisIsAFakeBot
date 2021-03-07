package com.thisisafakecom.thisisafakebot.commands.music;

import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class SkipCommand extends CommandAbstract {

  // matches a range i.e. 10 - 30, 5-3
  public String regex = "[0-9]+\\s*-\\s*[0-9]+";

  public SkipCommand() {
    commandHandled = "skip";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    TextChannel channel = input.getTextChannel();
    String[] tokenized = input.getContentRaw().split(" ", 2);
    if (tokenized.length == 1) {
      skipTrack(channel);
      input.getChannel().sendMessage("Skipped to next track.").queue();
    } else {
      if (tokenized[1].matches(regex)) {
        tokenized[1] = tokenized[1].replaceAll("\\s", "");
        int start = Integer.parseInt(tokenized[1].split("-")[0]);
        int end = Integer.parseInt(tokenized[1].split("-")[1]);
        if (start > end || start < 1) {
          throw new IncorrectUsageException();
        } else {
          // skip all songs from start to end (inclusive)
          MusicHandler mh = MusicHandler.getInstance();
          GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
          end = end > musicManager.scheduler.getNumSongsLeft() ? musicManager.scheduler.getNumSongsLeft() : end;
          musicManager.scheduler.removeRangeFromQueue(start, end);
          if (start == 1) {
            skipTrack(channel);
          }
          input.getChannel().sendMessage("Removed " + (end - start + 1) + " songs from queue.").queue();
        }
      } else {
        throw new IncorrectUsageException();
      }
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + " [Optional range of songs]``";
    channel.sendMessage(msg).queue();
  }

  private void skipTrack(TextChannel channel) {
    MusicHandler mh = MusicHandler.getInstance();
    GuildMusicManager musicManager = mh.getGuildAudioPlayer(channel.getGuild());
    musicManager.scheduler.nextTrack();
  }

  @Override
  public String commandDescription() {
    String ret = "Skips the currently playing song or skips a range of songs.\n" + "Usage: " + App.botPrefix
        + commandHandled + "[ Optional range of songs]";
    return ret;
  }
}
