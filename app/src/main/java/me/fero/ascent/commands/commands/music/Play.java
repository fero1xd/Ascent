package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.VeryBadDesign;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class Play implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        String prefix = VeryBadDesign.PREFIXES.get(ctx.getGuild().getIdLong());

        if(ctx.getArgs().isEmpty()) {
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            builder.setDescription("Correct usage is " + prefix + this.getName() + " <query/link>");
            channel.sendMessageEmbeds(builder.build()).queue();
        }

        String link = String.join(" ", ctx.getArgs());


        link = link.replace("<", "");
        link = link.replace(">", "");


        if (!isUrl(link)) {
            link = "ytsearch:" + link;
        }


        PlayerManager.getInstance().loadAndPlay(ctx, link, false, null);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getHelp() {
        return "Plays a song from youtube";
    }

    @Override
    public String getUsage() {
        return "play <track_name/link>";
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
    public List<String> getAliases() {
        return List.of("p");
    }
}