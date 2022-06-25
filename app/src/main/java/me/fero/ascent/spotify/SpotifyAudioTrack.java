package me.fero.ascent.spotify;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.fero.ascent.youtube.YoutubeAPI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class SpotifyAudioTrack extends YoutubeAudioTrack {

    private final String apiKey;
    private final SpotifyAudioSource sourceManager;

    private String youtubeId;

    public SpotifyAudioTrack(AudioTrackInfo trackInfo, String apiKey, SpotifyAudioSource sourceManager) {
        super(trackInfo, sourceManager.youtubeAudioSourceManager);
        this.apiKey = apiKey;
        this.sourceManager = sourceManager;
    }

    @Override
    public String getIdentifier() {
        if(this.youtubeId == null) {
            final AudioTrackInfo info = this.trackInfo;
            try {
                final List<SearchResult> results = YoutubeAPI.searchYoutubeIdOnly(info.title + ' ' + info.author, this.apiKey, 1L);

                if (results.isEmpty()) {
                    throw new FriendlyException("Failed to read info for " + info.uri, FriendlyException.Severity.SUSPICIOUS, null);
                }

                this.youtubeId = results.get(0).getId().getVideoId();
                // HACK: set the identifier on the trackInfo object
                this.setIdentifier(this.youtubeId);
            } catch (IOException e) {
                throw new FriendlyException("Failed to look up youtube track", FriendlyException.Severity.SUSPICIOUS, e);
            }
        }

        return this.youtubeId;
    }

    private void setIdentifier(String videoId) {
        final Class<AudioTrackInfo> infoCls = AudioTrackInfo.class;

        try {
            final Field identifier = infoCls.getDeclaredField("identifier");

            identifier.setAccessible(true);

            identifier.set(this.trackInfo, videoId);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new FriendlyException("Failed to look up youtube track", FriendlyException.Severity.SUSPICIOUS, e);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyAudioTrack(trackInfo, this.apiKey, sourceManager);
    }
}
