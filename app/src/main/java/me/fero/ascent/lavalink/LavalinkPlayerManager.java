package me.fero.ascent.lavalink;


import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;

public class LavalinkPlayerManager {
    private static LavalinkPlayerManager instance;
    private final AudioPlayerManager playerManager;
    private final HashMap<Long, GuildMusicManager> musicManagers;

    public LavalinkPlayerManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();

        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        synchronized(this) {
            long guildId = guild.getIdLong();
            GuildMusicManager mng = musicManagers.get(guildId);

            if (mng == null) {
                mng = new GuildMusicManager(guild);
                musicManagers.put(guildId, mng);
            }
            return mng;
        }
    }

    public void removeGuildMusicManager(Guild guild) {
        this.musicManagers.remove(guild.getIdLong());
    }


    public void loadAndPlay(CommandContext ctx, String query) {
        TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        if(musicManager.getScheduler().queue.size() >= musicManager.getScheduler().MAX_QUEUE_SIZE) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Max queue size reached", null, null, null).build()).queue();
            return;
        }

        if(SpotifyAudioSourceManager.INSTANCE.loadItem(ctx, query)) {
            return;
        }

        // TODO: SEARCH AND QUEUE YEYEYEYEYEYYEYEY ::)
    }

    public static LavalinkPlayerManager getInstance() {
        if(instance == null) {
            instance = new LavalinkPlayerManager();
        }
        return instance;
    }
}
