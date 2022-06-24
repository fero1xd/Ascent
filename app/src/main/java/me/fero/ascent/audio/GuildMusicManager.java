package me.fero.ascent.audio;


import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.lavalink.LavalinkManager;
import net.dv8tion.jda.api.entities.Guild;


public class GuildMusicManager {
    public final LavalinkPlayer player;
    private final TrackScheduler scheduler;

    public GuildMusicManager(Guild guild) {
        this.player = LavalinkManager.INS.createPlayer(guild.getIdLong());
        this.scheduler = new TrackScheduler(this.player, guild);
        this.player.addListener(this.scheduler);
    }

    public TrackScheduler getScheduler() {
        return this.scheduler;
    }

    public void stopAndClear() {
        final TrackScheduler scheduler = this.getScheduler();
        this.player.removeListener(scheduler);
        this.player.setPaused(false);

        if (this.player.getPlayingTrack() != null) {
            this.player.stopTrack();
        }

        scheduler.queue.clear();
    }
}