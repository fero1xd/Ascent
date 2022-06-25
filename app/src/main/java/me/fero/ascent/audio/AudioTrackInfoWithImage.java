package me.fero.ascent.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class AudioTrackInfoWithImage extends AudioTrackInfo {


    private final String image;

    /**
     * @param title      Track title
     * @param author     Track author, if known
     * @param length     Length of the track in milliseconds
     * @param identifier Audio source specific track identifier
     * @param isStream   True if this track is a stream
     * @param uri        URL of the track or path to its file.
     */
    public AudioTrackInfoWithImage(String title, String author, long length, String identifier, boolean isStream, String uri, String image) {
        super(title, author, length, identifier, isStream, uri);
        this.image = image;
    }


    public String getImage() {
        return image;
    }

}
