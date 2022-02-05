package me.fero.ascent.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    final public AudioPlayer player;

    public List<AudioTrack> queue = new ArrayList<>();
    public boolean isRepeating = false;

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
            return;
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
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
//        this.player.startTrack(track.makeClone(), false);

    }


}
