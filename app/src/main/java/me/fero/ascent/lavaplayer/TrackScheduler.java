package me.fero.ascent.lavaplayer;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;


public class TrackScheduler extends AudioEventAdapter {
    final public AudioPlayer player;

    public List<AudioTrack> queue = new ArrayList<>();
    public boolean isRepeating = false;
    public Guild currentGuild;
    public TextChannel bindedChannel;
    public Message lastSongEmbed;
    public List<Member> votes = new ArrayList<>();
    public List<Member> totalMembers = new ArrayList<>();
    public Boolean votingGoingOn = false;
    public int MAX_QUEUE_SIZE = 100;

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
    private final EqualizerFactory factory;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.currentGuild = guild;
        this.factory = new EqualizerFactory();

        this.player.setFilterFactory(this.factory);
        this.player.setFrameBufferDuration(500);
    }

    public void queue(AudioTrack track) {
        if(this.queue.size() >= MAX_QUEUE_SIZE) {
            return;
        }
        if(!this.player.startTrack(track, true)) {
            this.queue.add(track);
        }
    }

    public void nextTrack() {
        if(queue.isEmpty()) {
            this.player.startTrack(null, false);
            deleteLastSongEmbed();
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
        deleteLastSongEmbed();

        if(this.bindedChannel != null && this.currentGuild != null) {
            Member member = this.currentGuild.retrieveMemberById((long) track.getUserData()).complete();
            this.bindedChannel.sendMessageEmbeds(Embeds.songEmbed(member, track).setTitle("Now Started Playing <a:music:989100325522796544>").build())
                    .setActionRow(Embeds.getControls(true)).queue(this::setLastSongEmbed);
        }
    }

    public void deleteLastSongEmbed() {
        if(this.lastSongEmbed != null) {
            try {
                this.lastSongEmbed.delete().queue();
            } catch (Exception ignored) {}
            this.setLastSongEmbed(null);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            if(this.isRepeating) {
                this.player.startTrack(track.makeClone(), false);
                return;
            }
            if(this.queue.isEmpty() && currentGuild != null) {
                AudioManager audioManager = currentGuild.getAudioManager();
                audioManager.closeAudioConnection();
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
            this.player.startTrack(null, false);

            AudioManager audioManager = currentGuild.getAudioManager();
            audioManager.closeAudioConnection();
        }
        exception.printStackTrace();
    }

    public void setBindedChannel(TextChannel bindedChannel) {
        this.bindedChannel = bindedChannel;
    }

    public void setLastSongEmbed(Message lastSongEmbed) {
        this.lastSongEmbed = lastSongEmbed;
    }

    public boolean removeDuplicates() {
        if(queue.isEmpty() || queue.size() == 1) return false;

        // [1, 2, 3, 4, 5] total -> 4 indexes

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

    public void bassBoost(float percentage)
    {
        final float multiplier = percentage / 100.00f;

        for (int i = 0; i < BASS_BOOST.length; i++)
        {
            this.factory.setGain(i, BASS_BOOST[i] * multiplier);
        }
    }
}
