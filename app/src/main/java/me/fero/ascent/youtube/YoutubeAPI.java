package me.fero.ascent.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class YoutubeAPI {
   private static YouTube youtube;


   static {
       try {
           youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),null)
                   .setApplicationName("ascent-youtube-api").build();
       }
       catch (GeneralSecurityException | IOException e) {
           e.printStackTrace();
       }
   }

   public static List<SearchResult> searchYoutubeIdOnly(String query, String apiKey, long size) throws IOException {
       return youtube.search()
               .list("id")
               .setKey(apiKey)
               .setQ(query)
               .setType("video")
               .setMaxResults(size)
               .execute()
               .getItems();
   }

    public static List<SearchResult> searchYoutube(String query, String apiKey, long size) throws IOException {
        return youtube.search()
                .list("snippet")
                .setKey(apiKey)
                .setQ(query)
                .setType("video")
                .setFields("items(id/kind,id/videoId,snippet/title, snippet/channelTitle)")
                .setMaxResults(size)
                .execute()
                .getItems();
    }

    public static List<SearchResult> searchByCategory(String query, String apiKey, String categoryId, long size) throws IOException {
       return youtube.search()
               .list("snippet")
               .setKey(apiKey)
               .setType("video")
               .setMaxResults(size)
               .setFields("items(id/kind,id/videoId,snippet/title, snippet/channelTitle)")
               .setVideoCategoryId("categoryId")
               .execute()
               .getItems();
    }
}
