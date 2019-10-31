package com.thisisafakecom.thisisafakebot.commands.music.handlers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class MusicUtils {

	public static String getTrackTitleAndLength(AudioTrack track) {
		if (track == null) {
			return "No track!";
		}
		return track.getInfo().title + " " + convertSecondsToLengthString(convertMsToS(track.getInfo().length));
	}

	private static long convertMsToS(long ms) {
		return ms/1000;
	}

	private static String convertSecondsToLengthString(long seconds) {
		return seconds/60 + " min. " + seconds%60 + " sec.";
	}
}
