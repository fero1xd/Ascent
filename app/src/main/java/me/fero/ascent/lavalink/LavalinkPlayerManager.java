package me.fero.ascent.lavalink;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import me.fero.ascent.audio.AudioLoader;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.spotify.SpotifyAudioSource;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.concurrent.Future;

public class LavalinkPlayerManager {
    private static LavalinkPlayerManager instance;
    private final AudioPlayerManager playerManager;
    private final HashMap<Long, GuildMusicManager> musicManagers;

    public LavalinkPlayerManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();


        SpotifyAudioSource sp = (SpotifyAudioSource) SpotifyAudioSourceManager.INSTANCE;

        playerManager.registerSourceManager(sp.youtubeAudioSourceManager);
        playerManager.registerSourceManager(sp);

        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());

        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
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


    public Future<Void> loadAndPlay(CommandContext ctx, String query, boolean announceTracks) {
        TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        if(!musicManager.getScheduler().canQueue()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Max queue size reached", null, null, null).build()).queue();
            return null;
        }

        AudioLoader loader = new AudioLoader(ctx, musicManager, query, announceTracks);

        return getPlayerManager().loadItemOrdered(musicManager, query, loader);
    }

    public Future<Void> loadAndPlay(CommandContext ctx, String query, Message messageToDelete, boolean announceTracks) {
        TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        if(musicManager.getScheduler().queue.size() >= TrackScheduler.MAX_QUEUE_SIZE) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Max queue size reached", null, null, null).build()).queue();
            return null;
        }

        AudioLoader loader = new AudioLoader(ctx, musicManager, query, messageToDelete, announceTracks);

        return getPlayerManager().loadItemOrdered(musicManager, query, loader);
    }

    public static LavalinkPlayerManager getInstance() {
        if(instance == null) {
            instance = new LavalinkPlayerManager();
        }
        return instance;
    }
}
