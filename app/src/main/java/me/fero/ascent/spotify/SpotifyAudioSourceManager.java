package me.fero.ascent.spotify;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import me.fero.ascent.commands.setup.CommandContext;

public interface SpotifyAudioSourceManager {
    AudioSourceManager INSTANCE = new SpotifyAudioSource(new YoutubeAudioSourceManager());
    void searchTrack(CommandContext ctx, String query, EventWaiter waiter);
}
