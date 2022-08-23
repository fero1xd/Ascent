package me.fero.ascent.audio;


import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.io.filters.*;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import me.fero.ascent.exceptions.LimitReachedException;
import me.fero.ascent.lavalink.LavalinkManager;
import me.fero.ascent.spotify.SpotifyAudioTrack;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

public class TrackScheduler extends AudioEventAdapterWrapped {
    final public LavalinkPlayer player;

    public List<AudioTrack> queue = new ArrayList<>();
    public boolean isRepeating = false;

    public Guild currentGuild;
    public TextChannel bindedChannel;
    public Message lastSongEmbed;
    public List<Member> votes = new ArrayList<>();
    public List<Member> totalMembers = new ArrayList<>();
    public Boolean votingGoingOn = false;
    public static int MAX_QUEUE_SIZE = 100;

    public boolean karaokeMode = false;
    public boolean threedMode = false;

    private static final float[] BASS_BOOST = {
            0.2f,
            0.15f,
            0.1f,
            0.05f,
            0.0f,
            -0.05f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f
    };

    public TrackScheduler(LavalinkPlayer player, Guild guild) {
        this.player = player;
        this.currentGuild = guild;

        this.player.getFilters().setTimescale(new Timescale()).commit();
    }

    public boolean canQueue() {
        return this.queue.size() < MAX_QUEUE_SIZE;
    }

    public void addToQueue(AudioTrack track) throws LimitReachedException {
        if (queue.size() + 1 >= MAX_QUEUE_SIZE) {
            throw new LimitReachedException("The queue is full", MAX_QUEUE_SIZE);
        }

        if (player.getPlayingTrack() == null) {
            this.play(track);
        } else {
            queue.add(track);
        }
    }

    public void nextTrack() {
        if(queue.isEmpty()) {
            player.stopTrack();
            deleteLastSongEmbed();
        }
        else {
            try {
                this.play(queue.remove(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    // EVENTS
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.player.setPaused(false);

        deleteLastSongEmbed();

        if(this.bindedChannel != null && this.currentGuild != null) {
            Member member = this.currentGuild.retrieveMemberById((long) track.getUserData()).complete();
            this.bindedChannel.sendMessageEmbeds(Embeds.songEmbed(member, track).setTitle("Now Started Playing <a:music:989100325522796544>").build())
                    .setActionRow(Embeds.getControls(true)).queue((msg) -> {
                        this.lastSongEmbed = msg;
                    });
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if(this.isRepeating) {
                this.play(track.makeClone());
                return;
            }
            if(this.queue.isEmpty() && currentGuild != null) {
                LavalinkManager.INS.closeConnection(this.currentGuild);
                return;
            }
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if(currentGuild != null) {
            this.isRepeating = false;
            this.queue.clear();
            this.resetVotingSystem();
            this.deleteLastSongEmbed();
            this.player.stopTrack();

            LavalinkManager.INS.closeConnection(this.currentGuild);
        }

        final Throwable rootCause = ExceptionUtils.getRootCause(exception);
        final Throwable finalCause = rootCause == null ? exception : rootCause;
        final AudioTrackInfo info = track.getInfo();

        if (finalCause == null || finalCause.getMessage() == null) {
            if(this.bindedChannel != null) {
                this.bindedChannel.sendMessage("Something went terribly wrong when playing track with identifier `" + info.identifier +
                        "`\nPlease contact the developers asap with the identifier in the message above").queue();
            }
            return;
        }

        if (finalCause.getMessage().contains("Something went wrong when decoding the track.")) {
            return;
        }

        if (finalCause.getMessage().contains("age-restricted")) {
            if(this.bindedChannel != null) {
                this.bindedChannel.sendMessage("Cannot play `" + info.title + "` because it is age-restricted").queue();
            }
            return;
        }

        if(this.bindedChannel != null) {
            this.bindedChannel.sendMessage("Something went wrong while playing track with identifier `" +
                    info.identifier
                    + "`, please contact the devs if this happens a lot.\n" +
                    "Details: " + finalCause).queue();
        }

        exception.printStackTrace();
    }

    // Helpers
    private void play(AudioTrack track) {
        if(track instanceof SpotifyAudioTrack) {
            track.getIdentifier();
        }

        this.player.playTrack(track);
    }

    public void deleteLastSongEmbed() {
        if(this.lastSongEmbed != null) {
            try {
                this.lastSongEmbed.delete().queue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.lastSongEmbed = null;
        }
    }

    public boolean removeDuplicates() {
        if(queue.isEmpty() || queue.size() == 1) return false;

        List<String> uniqueIdentifiers = new ArrayList<>();
        List<AudioTrack> newQueue = new ArrayList<>();

        for(AudioTrack track : queue) {
            String identifier = track.getIdentifier();
            if(!uniqueIdentifiers.contains(identifier)) {
                uniqueIdentifiers.add(identifier);
                newQueue.add(track);
            }
        }

        boolean check = newQueue.size() != this.queue.size();

        this.queue = newQueue;
        return check;
    }

    public void initializeVotingSystem(List<Member> totalMembers) {
        this.votingGoingOn = true;
        this.votes.clear();
        this.totalMembers.clear();
        this.totalMembers = totalMembers;
    }

    public void resetVotingSystem() {
        this.votingGoingOn = false;
        this.votes.clear();
        this.totalMembers.clear();
    }

    // FILTERS
    public void bassBoost(float percentage)
    {
        final float multiplier = percentage / 100.00f;

        for (int i = 0; i < BASS_BOOST.length; i++)
        {
            this.player.getFilters().setBand(i, BASS_BOOST[i] * multiplier).commit();
        }
    }

    public void setSpeed(float percentage) {
        Filters filters = this.player.getFilters();
        Timescale timescale = filters.getTimescale();


        filters.setTimescale(timescale.setSpeed(percentage)).commit();
    }

    public void setPitch(float percentage) {
        Filters filters = this.player.getFilters();
        Timescale timescale = filters.getTimescale();


        filters.setTimescale(timescale.setPitch(percentage)).commit();
    }

    public void toggleRotation() {
        Filters filters = this.player.getFilters();
        Rotation rotation = filters.getRotation();

        if(rotation != null) {
            filters.setRotation(null).commit();
            threedMode = false;
        }
        else {
            filters.setRotation(new Rotation().setFrequency(0.2F)).commit();
            threedMode = true;
        }
    }

    public void toggleKaraoke() {
        Filters filters = this.player.getFilters();
        lavalink.client.io.filters.Karaoke karaoke = filters.getKaraoke();

        if(karaoke != null) {
            filters.setKaraoke(null).commit();
            karaokeMode = false;
        }
        else {
            filters.setKaraoke(new Karaoke()).commit();
            karaokeMode = true;
        }
    }
}
