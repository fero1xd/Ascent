package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlaying implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        final Member member = ctx.getMember();


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());


        AudioTrack playingTrack = musicManager.player.getPlayingTrack();

        if (playingTrack == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        EmbedBuilder builder = Embeds.songEmbed(member, playingTrack);
        builder.setTitle("Now playing 💿");

        long fullMillis = musicManager.player.getTrackPosition();

        String formattedFull;

        if(fullMillis >= 3600000) {
            formattedFull = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(fullMillis),
                    TimeUnit.MILLISECONDS.toMinutes(fullMillis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(fullMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(fullMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(fullMillis)));

        }
        else {
            formattedFull = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(fullMillis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(fullMillis)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(fullMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(fullMillis)));
        }

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
