package me.fero.ascent.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import me.fero.ascent.Config;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class YoutubeAPI {
    private YouTube youtube;
    private static YoutubeAPI ins;
    private static long NUMBER_OF_VIDEOS_RETURNED = 1L;

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();


    public YoutubeAPI() {
        try {
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("ascent-youtube-api").build();

            YouTube.Search.List search = youtube.search().list(List.of("id", "snippet"));

            search.setRelatedToVideoId("K7qXf0qnk5s");
            search.setType(List.of("video"));
            search.setKey(Config.get("youtube_data_api_key"));
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);


            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            PageInfo pageInfo = searchResponse.getPageInfo();
            String nextPageToken = searchResponse.getNextPageToken();
//            Integer resultsPerPage = pageInfo.getResultsPerPage();
//            System.out.println(resultsPerPage);
//            System.out.println(searchResponse.getNextPageToken());


            if (searchResultList != null) {
                Iterator<SearchResult> iteratorSearchResults = searchResultList.iterator();

                if (!iteratorSearchResults.hasNext()) {
                    System.out.println(" There aren't any results for your query.");
                }

                SearchResult singleVideo = iteratorSearchResults.next();
                ResourceId rId = singleVideo.getId();

                if(singleVideo.getSnippet() == null) {

                }
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }





    public static YoutubeAPI getInstance() {
        if(ins == null) {
            ins = new YoutubeAPI();
        }

        return ins;
    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }


        SearchResult singleVideo = iteratorSearchResults.next();
        ResourceId rId = singleVideo.getId();


        System.out.println(singleVideo);
            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
//            if (rId.getKind().equals("youtube#video") && singleVideo.getSnippet() != null) {
//                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
//
//                System.out.println(" Video Id - " + rId.getVideoId());
//                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
//                System.out.println(" Thumbnail: " + thumbnail.getUrl());
//                System.out.println("\n-------------------------------------------------------------\n");
//            }

    }
}
