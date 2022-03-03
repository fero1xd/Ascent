package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;
import java.util.List;

public class Spotify implements ICommand {

    private final EventWaiter waiter;

    public Spotify(EventWaiter waiter) {
        this.waiter = waiter;
    }
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();
        if(args.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please provide a query", null, null, null).build()).queue();
            return;
        }

        String q = String.join(" ", ctx.getArgs());


        q = q.replace("<", "");
        q = q.replace(">", "");


        if (isUrl(q)) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No links allowed here", null, null, null).build()).queue();
            return;
        }

        SpotifyAudioSourceManager.INSTANCE.searchTrack(ctx, q, this.waiter);
    }

    @Override
    public String getName() {
        return "spotify";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getHelp() {
        return "Searches spotify";
    }

    private boolean isUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int cooldownInSeconds() {
        return 5;
    }
}
