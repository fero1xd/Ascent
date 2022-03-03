package me.fero.ascent.spotify;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.fero.ascent.commands.CommandContext;


public interface SpotifyAudioSourceManager {
    SpotifyAudioSourceManager INSTANCE = new SpotifyAudioSource();

    boolean loadItem(CommandContext ctx, String url);
//    void getTrack(CommandContext event, String url);
//    void getPlaylist(CommandContext event, String url);
    void searchTrack(CommandContext ctx, String query, EventWaiter waiter);

}
