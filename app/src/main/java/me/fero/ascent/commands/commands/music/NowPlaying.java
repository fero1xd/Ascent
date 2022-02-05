package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlaying implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        final Member member = ctx.getMember();


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;
        AudioTrack playingTrack = audioPlayer.getPlayingTrack();
        if (playingTrack == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }


        EmbedBuilder builder = Embeds.songEmbed(member, playingTrack);



        long fullMillis = playingTrack.getPosition();
        String formattedFull = String.format("%02d:%02d",

                TimeUnit.MILLISECONDS.toMinutes(fullMillis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(fullMillis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(fullMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(fullMillis)));

        if(!playingTrack.getInfo().isStream) {
            builder.addField("Current position", formattedFull + " minutes", false);
        }


        channel.sendMessageEmbeds(builder.build()).queue();
    }
    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getHelp() {
        return "Shows the currently playing song";
    }

    @Override
    public List<String> getAliases() {
        return List.of("np","pb");
    }

    @Override
    public String getType() {
        return "music";
    }
}
