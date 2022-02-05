package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.VeryBadDesign;
import me.fero.ascent.lavaplayer.PlayerManager;

import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;

public class ScPlay implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        String prefix = VeryBadDesign.PREFIXES.get(ctx.getGuild().getIdLong());

        if(ctx.getArgs().isEmpty()) {
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            builder.setDescription("Correct usage is " + prefix + this.getName() + " <query>");
            channel.sendMessageEmbeds(builder.build()).queue();
        }


        String link = String.join(" ", ctx.getArgs());


        link = link.replace("<", "");
        link = link.replace(">", "");


        if (isUrl(link)) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No links allowed", null, null, null).build()).queue();
            return;
        }

        link = "scsearch:" + link;

        PlayerManager.getInstance().loadAndPlay(ctx, link, false, null);
    }

    @Override
    public String getName() {
        return "scplay";
    }

    @Override
    public String getHelp() {
        return "Plays music from soundcloud";
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
        return "scplay <track_name>";
    }
}
