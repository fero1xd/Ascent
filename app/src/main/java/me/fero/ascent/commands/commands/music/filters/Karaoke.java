package me.fero.ascent.commands.commands.music.filters;

import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class Karaoke implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Guild guild = ctx.getGuild();

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(guild);

        if(musicManager.player.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            return;
        }

        TrackScheduler scheduler = musicManager.getScheduler();
        scheduler.toggleKaraoke();

        channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Karaoke mode is now " + (scheduler.karaokeMode ? "on" : "off"),
                "NOTE - This may take a while", null, null).build()).queue();
    }

    @Override
    public String getName() {
        return "karaoke";
    }

    @Override
    public String getHelp() {
        return "Toggles karaoke mode";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public boolean isDjNeeded() {
        return true;
    }

    @Override
    public int cooldownInSeconds() {
        return 10;
    }
}
