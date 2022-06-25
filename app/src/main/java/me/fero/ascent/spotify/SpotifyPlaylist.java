package me.fero.ascent.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

import java.util.List;

public class SpotifyPlaylist extends BasicAudioPlaylist {

    private final int originalSize;

    public SpotifyPlaylist(String name, List<AudioTrack> tracks, AudioTrack selectedTrack, boolean isSearchResult, int originalSize) {
        super(name, tracks, selectedTrack, isSearchResult);
        this.originalSize = originalSize;
    }

    public int getOriginalSize() {
        return this.originalSize;
    }

    public boolean isBig() {
        return originalSize > getTracks().size();
    }
}
