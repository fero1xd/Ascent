package me.fero.ascent.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;


public class TrackScheduler extends AudioEventAdapter {
    final public AudioPlayer player;

    public List<AudioTrack> queue = new ArrayList<>();
    public boolean isRepeating = false;
    public TextChannel cachedChannel = null;


    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public void queue(AudioTrack track) {

        if(!this.player.startTrack(track, true)) {
            this.queue.add(track);
        }
    }

    public void nextTrack() {

        if(queue.isEmpty()) {
            this.player.startTrack(null, false);
            return;
        }

        try {
            this.player.startTrack(queue.remove(0), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.player.setPaused(false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if(this.isRepeating) {
                this.player.startTrack(track.makeClone(), false);
                return;
            }
            if(this.queue.isEmpty() && cachedChannel != null) {
                this.isRepeating = false;
                this.queue.clear();
                this.player.stopTrack();

                AudioManager audioManager = cachedChannel.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
                return;
            }
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if(this.cachedChannel != null) {
            this.cachedChannel.sendMessageEmbeds(EmbedUtils.getDefaultEmbed().setDescription("Track loading Failed, try again later !").build()).queue();
        }
        exception.printStackTrace();
    }


}
