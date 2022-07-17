package me.fero.ascent.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class AudioLoader implements AudioLoadResultHandler {

    private final CommandContext ctx;
    private final TextChannel channel;
    private final GuildMusicManager musicManager;
    private final String query;
    private Message messageToDelete = null;
    private final boolean announceTracks;

    public AudioLoader(CommandContext ctx, GuildMusicManager musicManager, String query, boolean announceTracks) {
        this.ctx = ctx;
        this.channel = ctx.getChannel();
        this.musicManager = musicManager;
        this.query = query;
        this.announceTracks = announceTracks;
    }

    public AudioLoader(CommandContext ctx, GuildMusicManager musicManager, String query, Message messageToDelete, boolean announceTracks) {
        this.ctx = ctx;
        this.channel = ctx.getChannel();
        this.musicManager = musicManager;
        this.query = query;
        this.messageToDelete = messageToDelete;
        this.announceTracks = announceTracks;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if(!this.musicManager.getScheduler().canQueue()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Queue is full", null, null, null).build()).queue();
            return;
        }

        track.setUserData(ctx.getAuthor().getIdLong());

        TrackScheduler scheduler = musicManager.getScheduler();

        if(this.messageToDelete != null) {
            this.messageToDelete.delete().queue();
        }

        scheduler.addToQueue(track);

        if(scheduler.queue.size() > 0 && this.announceTracks) {
            channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(track).build()).queue();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if(!this.musicManager.getScheduler().canQueue()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Queue is full", null, null, null).build()).queue();
            return;
        }

        final List<AudioTrack> tracks = playlist.getTracks();
        TrackScheduler scheduler = musicManager.getScheduler();

        if(tracks.isEmpty()) {
            this.noMatches();
            return;
        }

        if(!playlist.isSearchResult()) {
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            String description = "Loading **[" + playlist.getName() + "]" + "(" + query + ")** in the queue";

            builder.setDescription(description);

            channel.sendMessageEmbeds(builder.build())
                    .queue();

            for(AudioTrack track : tracks) {
                track.setUserData(ctx.getAuthor().getIdLong());
                scheduler.addToQueue(track);
            }
            return;
        }


        final AudioTrack track = tracks.get(0);
        track.setUserData(ctx.getAuthor().getIdLong());

        scheduler.addToQueue(track);

        if(scheduler.queue.size() > 0 && this.announceTracks) {
            channel.sendMessageEmbeds(Embeds.songEmbedWithoutDetails(track).build()).queue();
        }
    }

    @Override
    public void noMatches() {
        if(!this.announceTracks) return;

        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setTitle("Error!");
        builder.setDescription("❌ No tracks found");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if(!this.announceTracks) return;
        channel.sendMessageEmbeds(EmbedUtils.getDefaultEmbed().setDescription("Track loading Failed, try again later !").build()).queue();
    }
}
