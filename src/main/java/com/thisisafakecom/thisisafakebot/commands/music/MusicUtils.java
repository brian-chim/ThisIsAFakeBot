package com.thisisafakecom.thisisafakebot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class MusicUtils {

	public static String getTrackTitleAndLength(AudioTrack track) {
		return track.getInfo().title + " " + track.getInfo().length;
	}

	private static long convertMsToS(long ms) {
		return ms/1000;
	}
}
