package me.fero.ascent.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        //  Creates a new Audio Player
        this.audioPlayer = manager.createPlayer();

        this.scheduler = new TrackScheduler(this.audioPlayer, guild);

        // Adds scheduler as event listener
        this.audioPlayer.addListener(this.scheduler);
        // Make a send handler for audio
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }


    public AudioPlayerSendHandler getSendHandler() {
        return this.sendHandler;
    }
}
