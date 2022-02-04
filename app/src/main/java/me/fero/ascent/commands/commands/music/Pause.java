package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class Pause implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(@NotNull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member member =  ctx.getMember();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;
        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        AudioPlayer player = musicManager.scheduler.player;
        if(player.isPaused()) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Player is already paused", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);

            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        player.setPaused(true);

        EmbedBuilder builder = Embeds.createBuilder(null, "Player paused", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);

        channel.sendMessageEmbeds(builder.build()).queue();

    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getHelp() {
        return "Pauses the current song";
    }


    @Override
    public String getType() {
        return "music";
    }
}
