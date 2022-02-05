package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.PlayerManager;

import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;

public class Search implements ICommand {
    final private EventWaiter waiter;

    public Search(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        if(ctx.getArgs().isEmpty()) {
            channel.sendMessage("Correct usage is " + Config.get("prefix") + "play <youtube_link>/").queue();
        }

        String link = String.join(" ", ctx.getArgs());


        link = link.replace("<", "");
        link = link.replace(">", "");

        if (isUrl(link)) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No links allowed", null, null, null).build()).queue();
            return;
        }

        link = "ytsearch:" + link;


        PlayerManager.getInstance().loadAndPlay(ctx, link, true, waiter);
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getHelp() {
        return "Searches song";
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
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage() {
        return "search <track_name>";
    }
}
