package me.fero.ascent.lavaplayer;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.Listener;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.w3c.dom.Text;

import javax.annotation.Nullable;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    public final AudioPlayerManager audioPlayerManager;


    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
           final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
           guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
           return guildMusicManager;
        });
    }

    public void loadAndPlay(CommandContext ctx, String query, boolean isSearchCmd, EventWaiter waiter) {

        TextChannel channel = ctx.getChannel();
        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();



                // User Loaded playlist
                if(!playlist.isSearchResult()) {
                    EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                    builder.setFooter("Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl());
                    builder.setTitle("Added to queue 💿");
                    String description = "[" + playlist.getName() + "]" + "(" + query + ")";
                    builder.setDescription(description);

                    channel.sendMessageEmbeds(builder.build())
                            .queue();
                    for(AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                    return;
                }

                // Search feature
                if(tracks.size() > 1 && isSearchCmd && waiter != null) {
                    EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                    builder.setTitle("Select a track track");
                    builder.setFooter("Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl());

                    final int trackCount = Math.min(tracks.size(), 10);

                    for (int i = 0; i <  trackCount; i++) {
                        final AudioTrack track = tracks.get(i);
                        final AudioTrackInfo info = track.getInfo();

                        builder.appendDescription(i+1 + ". `" + info.title + " by " + info.author + "`"+ "\n");
                    }
                    channel.sendMessageEmbeds(builder.build()).queue((message -> {
                        waiter.waitForEvent(
                                GuildMessageReceivedEvent.class,
                                (e) -> e.getChannel() == channel &&  e.getMember() == ctx.getMember() && !e.getAuthor().isBot(),
                                (e) -> {
                                    String inputRaw = e.getMessage().getContentRaw();

                                    int input = -1;
                                    try {
                                        input = Integer.parseInt(inputRaw);

                                    } catch (NumberFormatException ex) {
                                        message.delete().queue();
                                        e.getMessage().delete().queue();
                                        return;
                                    }

                                    if(input <= 0 || input > trackCount) {
                                        message.delete().queue();
                                        e.getMessage().delete().queue();
                                        final AudioTrack track = tracks.get(0);
                                        channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();


                                        musicManager.scheduler.queue(track);
                                        return;
                                    }

                                    AudioTrack track = null;
                                    try {
                                        track = tracks.get(input - 1);
                                    }catch (IndexOutOfBoundsException exc) {
                                        final AudioTrack trackx = tracks.get(0);
                                        message.delete().queue();
                                        e.getMessage().delete().queue();
                                        channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), trackx).build()).queue();

                                        musicManager.scheduler.queue(trackx);
                                        return;
                                    }

                                    if(track != null) {
                                        message.delete().queue();
                                        e.getMessage().delete().queue();
                                        channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();

                                        musicManager.scheduler.queue(track);
                                    }

                                },
                                15L, TimeUnit.SECONDS,
                                () -> {
                                    message.delete().queue();
                                    final AudioTrack track = tracks.get(0);
                                    channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();


                                    musicManager.scheduler.queue(track);
                                }

                        );
                    }));

                    return;

                }


                final AudioTrack track = tracks.get(0);
                channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();

                musicManager.scheduler.queue(track);
            }

            @Override
            public void noMatches() {

                Listener.LOGGER.error("No Tracks Found");

                EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                builder.setTitle("Error!");
                builder.setDescription("❌ No tracks found");
                channel.sendMessageEmbeds(builder.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessageEmbeds(EmbedUtils.getDefaultEmbed().setDescription("Track loading Failed, try again later !").build()).queue();
            }
        });
    }


    public void queueMultipleUrl(CommandContext ctx, List<String> urls) {
        TextChannel channel = ctx.getChannel();
        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        for(String url : urls) {
            this.audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException exception) {

                }
            });

        }
    }

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }

        return instance;
    }

}
