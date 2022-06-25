package me.fero.ascent.commands.commands.music;

import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Restart implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());

        LavalinkPlayer audioPlayer = musicManager.player;

        if(audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            return;
        }

        audioPlayer.setPaused(false);
        audioPlayer.seekTo(0);


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
