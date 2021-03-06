package com.thisisafakecom.thisisafakebot.commands.music.handlers;

/**
 * Sample Java code for youtube.search.list
 * Taken from the Youtube list docs
 * https://developers.google.com/youtube/v3/docs
 * See instructions for running these code samples locally:
 * https://developers.google.com/explorer-help/guides/code_samples#java
 */

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YoutubeHandler {
	private static final String DEVELOPER_KEY = getYoutubeKey();

	private static final String APPLICATION_NAME = "API code samples";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/**
	 * Build and return an authorized API client service.
	 *
	 * @return an authorized API client service
	 * @throws GeneralSecurityException, IOException
	 */
	public static YouTube getService() throws GeneralSecurityException, IOException {
		final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new YouTube.Builder(httpTransport, JSON_FACTORY, null).setApplicationName(APPLICATION_NAME).build();
	}

	private static String getYoutubeKey() {
		try {
			return new String(Files.readAllBytes(Paths.get("youtube.secret")));
		} catch (IOException e) {
			System.out.println("Failed to retrieve YouTube token.");
			return "";
		}
	}

	public static ArrayList<YoutubeSearchInfo> searchRelatedToVideo(String id)
	    throws GeneralSecurityException, IOException, GeneralSecurityException {
	  YouTube youtubeService = getService();
	  YouTube.Search.List listVids = youtubeService.search().list("snippet");
	  SearchListResponse listVidsResp = listVids.setKey(DEVELOPER_KEY).setMaxResults(20L).setRelatedToVideoId(id).setType("video").execute();
	   ArrayList<YoutubeSearchInfo> returnInfo = new ArrayList<YoutubeSearchInfo>();
	   List<SearchResult> listVidResults = listVidsResp.getItems();
	    String vidIds = "";
	    for (SearchResult result : listVidResults) {
	      vidIds += result.getId().getVideoId() + ",";
	    }
	    vidIds = vidIds.substring(0, vidIds.length() - 1);
	    YouTube.Videos.List listVidInfo = youtubeService.videos().list("snippet,contentDetails");
	    VideoListResponse listVidInfoResp = listVidInfo.setKey(DEVELOPER_KEY).setId(vidIds).execute();
	    HashMap<String, String> vidAndDuration = new HashMap<>();
	    for (Video vid : listVidInfoResp.getItems()) {
	      vidAndDuration.put(vid.getId(), vid.getContentDetails().getDuration());
	    }
	    for (SearchResult result : listVidResults) {
	      String vidId = result.getId().getVideoId();
	      String vidTitle;
	      try {
	        vidTitle = result.getSnippet().getTitle();
	        // occasionally, the results seem to not have a snippet attribute so just skip over it
	        // this doesnt seem to happen when simply searching for a video
	      } catch (NullPointerException e) {
	        continue;
	      }
	      String vidLength = vidAndDuration.get(vidId);
	      returnInfo.add(new YoutubeSearchInfo(vidId, vidTitle, vidLength));
	    }
    return returnInfo;
	}

	public static ArrayList<YoutubeSearchInfo> searchVideos(String query)
			throws GeneralSecurityException, IOException, GoogleJsonResponseException {
		YouTube youtubeService = getService();
		YouTube.Search.List listVids = youtubeService.search().list("snippet");
		SearchListResponse listVidsResp = listVids.setKey(DEVELOPER_KEY).setMaxResults(5L).setQ(query).setType("video").execute();
		ArrayList<YoutubeSearchInfo> returnInfo = new ArrayList<YoutubeSearchInfo>();
		List<SearchResult> listVidResults = listVidsResp.getItems();
		String vidIds = "";
		for (SearchResult result : listVidResults) {
			vidIds += result.getId().getVideoId() + ",";
		}
		vidIds = vidIds.substring(0, vidIds.length() - 1);
		YouTube.Videos.List listVidInfo = youtubeService.videos().list("snippet,contentDetails");
		VideoListResponse listVidInfoResp = listVidInfo.setKey(DEVELOPER_KEY).setId(vidIds).execute();
		HashMap<String, String> vidAndDuration = new HashMap<>();
		for (Video vid : listVidInfoResp.getItems()) {
			vidAndDuration.put(vid.getId(), vid.getContentDetails().getDuration());
		}
		for (SearchResult result : listVidResults) {
			returnInfo.add(new YoutubeSearchInfo(result.getId().getVideoId(), result.getSnippet().getTitle(), vidAndDuration.get(result.getId().getVideoId())));
		}
		return returnInfo;
	}
}