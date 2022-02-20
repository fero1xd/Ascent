package me.fero.ascent.spotify;
import me.fero.ascent.commands.CommandContext;


public interface SpotifyAudioSourceManager {
    SpotifyAudioSourceManager INSTANCE = new SpotifyAudioSource();

    boolean loadItem(CommandContext ctx, String url);
//    void getTrack(CommandContext event, String url);
//    void getPlaylist(CommandContext event, String url);

}
