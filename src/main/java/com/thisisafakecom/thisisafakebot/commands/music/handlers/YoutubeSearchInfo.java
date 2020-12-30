package com.thisisafakecom.thisisafakebot.commands.music.handlers;

public class YoutubeSearchInfo {

	public String videoId;
	public String videoTitle;
	public String videoLength;
	
	public YoutubeSearchInfo(String videoId, String videoTitle, String duration) {
		this.videoId = videoId;
		this.videoTitle = videoTitle;
		this.videoLength = duration;
	}

}
