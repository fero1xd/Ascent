package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class Resume implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(@NotNull CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;
        if(audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            return;
        }

        AudioPlayer player = musicManager.scheduler.player;
        if(!player.isPaused()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Player is already playing", null, null, null).build()).queue();
            return;
        }

        player.setPaused(false);
        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Player resumed", null, null, null).build()).queue();

    }


    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getHelp() {
        return "Resumes the current track.";
    }

    @Override
    public String getType() {
        return "music";
    }

}
