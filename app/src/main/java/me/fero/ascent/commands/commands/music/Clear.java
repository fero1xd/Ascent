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

import java.util.List;

public class Clear implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        TrackScheduler scheduler = musicManager.getScheduler();

        final List<AudioTrack> queue = scheduler.queue;

        if (queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();
            return;
        }

        scheduler.queue.clear();

        EmbedBuilder builder = Embeds.createBuilder(null, "💥 The queue has been cleared", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getHelp() {
        return "Clears the queue";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("cls");
    }

    @Override
    public boolean isDjNeeded() {
        return true;
    }

}
