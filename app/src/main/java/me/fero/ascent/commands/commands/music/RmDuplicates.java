package me.fero.ascent.commands.commands.music;

import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

public class RmDuplicates implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        TrackScheduler scheduler = musicManager.getScheduler();

        if(scheduler.queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Queue is currently empty", null, null, null).build()).queue();
            return;
        }

        if(scheduler.removeDuplicates()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Removed duplicates from the queue.", null, null, null).build()).queue();
            return;
        }

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "No duplicates found", null, null,null).build()).queue();

    }

    @Override
    public String getName() {
        return "rmduplicates";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getHelp() {
        return "Removes duplicate tracks in the queue";
    }
}
