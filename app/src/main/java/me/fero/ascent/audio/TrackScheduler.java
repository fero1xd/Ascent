package me.fero.ascent.audio;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import me.fero.ascent.exceptions.LimitReachedException;
import me.fero.ascent.lavalink.LavalinkManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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
    public int MAX_QUEUE_SIZE = 100;

    public TrackScheduler(LavalinkPlayer player, Guild guild) {
        this.player = player;
        this.currentGuild = guild;
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
                    .setActionRow(Embeds.getControls(true)).queue((msg) -> this.lastSongEmbed = msg);
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
        if(currentGuild!=null) {

            this.isRepeating = false;
            this.queue.clear();
            this.player.stopTrack();

            LavalinkManager.INS.closeConnection(this.currentGuild);
        }
        exception.printStackTrace();
    }

    // Helpers
    private void play(AudioTrack track) {
        this.player.playTrack(track);
    }

    public void deleteLastSongEmbed() {
        if(this.lastSongEmbed != null) {
            try {
                this.lastSongEmbed.delete().queue();
            } catch (Exception ignored) {}

            this.lastSongEmbed = null;
        }
    }
}
