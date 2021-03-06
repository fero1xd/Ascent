package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;
import java.util.List;

public class YtmPlay implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());

        if(ctx.getArgs().isEmpty()) {
            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            builder.setDescription("Correct usage is " + prefix + this.getName() + " <query/link>");
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }


        String link = String.join(" ", ctx.getArgs());


        link = link.replace("<", "");
        link = link.replace(">", "");


        if (!isUrl(link)) {
            link = "ytmsearch:" + link;

            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, link, true);
        }
        else {
            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, link, true);
        }

    }

    @Override
    public String getName() {
        return "ytmplay";
    }

    @Override
    public String getHelp() {
        return "Plays a song from youtube music";
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "ytmplay <track_name/link>";
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
        return List.of("ytmp");
    }

    @Override
    public int cooldownInSeconds() {
        return 5;
    }

    @Override
    public boolean mayAutoJoin() {
        return true;
    }
}
