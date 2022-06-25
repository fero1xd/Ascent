package me.fero.ascent.commands.commands.music;

import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class Stop implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        TrackScheduler scheduler = musicManager.getScheduler();

        if(musicManager.player.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        scheduler.queue.clear();
        scheduler.player.stopTrack();
        scheduler.isRepeating = false;
        scheduler.resetVotingSystem();

        EmbedBuilder builder = Embeds.createBuilder(null, "💥 Player stopped and cleared the queue", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getHelp() {
        return "Stops the current song and clears the queue";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public boolean isDjNeeded() {
        return true;
    }
}
