package me.fero.ascent.commands.commands.music;

import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.regex.*;

public class Seek implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        LavalinkPlayer audioPlayer = musicManager.player;

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

        if(!audioPlayer.getPlayingTrack().isSeekable()) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "This track is not seekable", null, null, null);
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
        audioPlayer.seekTo(secs * 1000);

        ctx.getMessage().addReaction("👍").queue();
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
