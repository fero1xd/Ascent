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
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Long, GuildMusicManager> musicManagers;
    public final AudioPlayerManager audioPlayerManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);


    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
           final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);
           guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
           return guildMusicManager;
        });
    }

    public void removeGuildMusicManager(Guild guild) {
        this.musicManagers.remove(guild.getIdLong());
    }

    public void loadAndPlay(CommandContext ctx, String query, boolean isSearchCmd, EventWaiter waiter) {

        TextChannel channel = ctx.getChannel();
        GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        if(musicManager.scheduler.queue.size() >= musicManager.scheduler.MAX_QUEUE_SIZE) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Max queue size reached", null, null, null).build()).queue();
            return;
        }

        if(SpotifyAudioSourceManager.INSTANCE.loadItem(ctx, query)) {
            return;
        }

        this.audioPlayerManager.loadItemOrdered(musicManager, query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(ctx.getAuthor().getIdLong());
                musicManager.scheduler.queue(track);

                if(musicManager.scheduler.queue.size() > 0) {
                    channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(track).build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                // User Loaded playlist
                if(!playlist.isSearchResult()) {
                    EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                    String description = "Loading **[" + playlist.getName() + "]" + "(" + query + ")** in the queue";
                    builder.setDescription(description);


                    channel.sendMessageEmbeds(builder.build())
                            .queue();

                    for(AudioTrack track : tracks) {
                        track.setUserData(ctx.getAuthor().getIdLong());
                        musicManager.scheduler.queue(track);
                    }
                    return;
                }

                // Search feature
                if(tracks.size() > 1 && isSearchCmd && waiter != null) {
                    EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                    builder.setDescription("Select a track. You have 15 seconds");

                    final int trackCount = Math.min(tracks.size(), 20);
                    String s = UUID.randomUUID().toString();
                    SelectionMenu.Builder menu = SelectionMenu.create(s);
                    menu.setPlaceholder("Select your track here");
                    menu.setRequiredRange(1, 1);

                    for (int i = 0; i <  trackCount; i++) {
                        final AudioTrack track = tracks.get(i);
                        final AudioTrackInfo info = track.getInfo();
                        menu.addOption(info.title, String.valueOf(tracks.indexOf(track)), info.author);
                    }
                    channel.sendMessageEmbeds(builder.build()).setActionRow(menu.build()).queue((message -> {
                        waiter.waitForEvent(
                                SelectionMenuEvent.class,
                                (e) -> {
                                    if(!(e.getChannel() == channel && e.getMember() != null && !e.getMember().getUser().isBot() && e.getComponentId().equals(s))) {
                                        return false;
                                    }
                                    if(e.getMember() != ctx.getMember()) {
                                        e.reply("This menu is not for you").setEphemeral(true).queue();
                                        return false;
                                    }
                                    return true;
                                },
                                (e) -> {
                                    int index = Integer.parseInt(e.getValues().get(0));
                                    AudioTrack audioTrack = tracks.get(index);
                                    if(audioTrack!=null) {
                                        message.delete().queue();

                                        audioTrack.setUserData(ctx.getAuthor().getIdLong());
                                        musicManager.scheduler.queue(audioTrack);

                                        if(musicManager.scheduler.queue.size() > 0) {
                                            channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(audioTrack).build()).queue();
                                        }
//                                        else {
//                                            Embeds.sendSongEmbed(ctx.getMember(), audioTrack, channel);
//                                        }
                                    }

                                },
                                15, TimeUnit.SECONDS,
                                () -> {
                                    message.delete().queue();
                                    final AudioTrack track = tracks.get(0);

                                    track.setUserData(ctx.getAuthor().getIdLong());
                                    musicManager.scheduler.queue(track);

                                    if(musicManager.scheduler.queue.size() > 0) {
                                        channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(track).build()).queue();
                                    }
//                                    else {
//                                        Embeds.sendSongEmbed(ctx.getMember(), track, channel);
//                                    }
                                }
                        );
                    }));
                    return;
                }

                final AudioTrack track = tracks.get(0);

                track.setUserData(ctx.getAuthor().getIdLong());
                musicManager.scheduler.queue(track);

                if(musicManager.scheduler.queue.size() > 0) {
                    channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(track).build()).queue();
                }
//                else {
//                    Embeds.sendSongEmbed(ctx.getMember(), track, channel);
//                }
            }

            @Override
            public void noMatches() {
                LOGGER.error("No Tracks Found");

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

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

}
