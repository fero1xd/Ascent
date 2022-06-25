package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Collections;
import java.util.List;

public class Shuffle implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());

        TrackScheduler scheduler = musicManager.getScheduler();
        List<AudioTrack> queue = scheduler.queue;

        if(queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();
            return;
        }

        Collections.shuffle(scheduler.queue);
        EmbedBuilder builder = Embeds.createBuilder(null, "Queue shuffled", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp() {
        return "Shuffles the queue";
    }

    @Override
    public String getType() {
        return "music";
    }
}
