package com.thisisafakecom.thisisafakebot.commands.music;

import java.util.ArrayList;
import java.util.LinkedList;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.thisisafakecom.thisisafakebot.App;
import com.thisisafakecom.thisisafakebot.commands.CommandAbstract;
import com.thisisafakecom.thisisafakebot.commands.IncorrectUsageException;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.GuildMusicManager;
import com.thisisafakecom.thisisafakebot.commands.music.handlers.MusicHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ExportYTPCommand extends CommandAbstract {

  public ExportYTPCommand() {
    commandHandled = "exportytp";
  }

  @Override
  public void handle(Message input) throws IncorrectUsageException {
    String[] tokenized = input.getContentRaw().split(" ");
    if (tokenized.length != 1) {
      throw new IncorrectUsageException();
    }
    MusicHandler mh = MusicHandler.getInstance();
    GuildMusicManager gm = mh.getGuildAudioPlayer(input.getGuild());
    ArrayList<AudioTrack> tracks = gm.scheduler.getAllTracks();
    LinkedList<String> msgs = new LinkedList<String>();
    String msg = "";
    for (AudioTrack track : tracks) {
      // in case there are too many songs
      if (msg.length() >= Message.MAX_CONTENT_LENGTH) {
        msgs.add(msg);
        msg = "";
      }
      msg += track.getInfo().identifier + ";";
    }
    msgs.add(msg);
    MessageChannel channel = input.getChannel();
    if (msg.isEmpty()) {
      msg = "No tracks in queue!";
      channel.sendMessage(msg).queue();
    } else {
      for (String m : msgs) {
        m = "```" + m + "```";
        channel.sendMessage(m).queue();
      }
    }
  }

  @Override
  public void correctUsage(Message input) {
    MessageChannel channel = input.getChannel();
    String msg = "Correct Usage: ``" + App.botPrefix + commandHandled + "``";
    channel.sendMessage(msg).queue();
  }

  @Override
  public String commandDescription() {
    String ret = "Exports the current playlist (assuming everything is from youtube).\n"
        + "Copy and paste the output into the importytp command to reload the playlist.\n" + "Usage: " + App.botPrefix
        + commandHandled;
    return ret;
  }
}
