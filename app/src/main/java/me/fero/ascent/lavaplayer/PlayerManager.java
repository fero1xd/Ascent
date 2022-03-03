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
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.*;
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
                musicManager.scheduler.queue(track);
                track.setUserData(ctx.getAuthor().getIdLong());
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
                                        channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), audioTrack).build()).queue();
                                        audioTrack.setUserData(ctx.getAuthor().getIdLong());
                                        musicManager.scheduler.queue(audioTrack);
                                    }

                                },
                                15, TimeUnit.SECONDS,
                                () -> {
                                    message.delete().queue();
                                    final AudioTrack track = tracks.get(0);
                                    channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();

                                    track.setUserData(ctx.getAuthor().getIdLong());

                                    musicManager.scheduler.queue(track);
                                }
                        );
                    }));
                    return;
                }

                final AudioTrack track = tracks.get(0);
                channel.sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), track).build()).queue();
                track.setUserData(ctx.getAuthor().getIdLong());

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

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }

        return instance;
    }

}
