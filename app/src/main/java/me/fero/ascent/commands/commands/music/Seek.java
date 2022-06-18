package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.Config;
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
import java.util.regex.*;

public class Seek implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.scheduler.player;

        if(ctx.getArgs().isEmpty()) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Right format is MM:SS", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();

            return;
        }

        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        String arg = ctx.getArgs().get(0);

        Matcher match = Pattern.compile("([0-9]{1,2})[:ms](([0-9]{1,2})s?)?").matcher(arg);
        if(!match.matches()) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Right format is MM:SS", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();

            return;
        }


        long secs = -1;
        if(match.group(3).length() > 0) {
            int g1 = Integer.parseInt(match.group(1)) * 60;
            int g2 = Integer.parseInt(match.group(3));

            secs = g1 + g2;
        }
        else {
            secs = Integer.parseInt(match.group(1));
        }

        if(secs < 0) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Error formatting timestamp !", null, null, null);

            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        audioPlayer.setPaused(false);
        audioPlayer.getPlayingTrack().setPosition(secs * 1000);

        EmbedBuilder builder = Embeds.createBuilder(null, "Sought", null, null, null);

        channel.sendMessageEmbeds(builder.build()).queue();

    }

    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String getHelp() {
        return "Seeks the current track to the specified position";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "seek <time_stamp>";
    }

    @Override
    public int cooldownInSeconds() {
        return 5;
    }
}
