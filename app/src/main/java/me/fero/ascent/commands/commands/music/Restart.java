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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Restart implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.scheduler.player;
        if(audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            return;
        }

        audioPlayer.setPaused(false);

        audioPlayer.getPlayingTrack().setPosition(0);


        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track starting from beginning", null, null, null).build()).queue();

    }

    @Override
    public String getName() {
        return "restart";
    }

    @Override
    public String getHelp() {
        return "Restarts the current playing song";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rt");
    }
}
