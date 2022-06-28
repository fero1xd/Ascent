package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;
import java.util.List;


public class Play implements ICommand {

    private List<String> acceptedExtensions = List.of("wav", "mkv", "mp4", "flac", "ogg", "mp3", "aac", "ts");

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());

        if(!ctx.getMessage().getAttachments().isEmpty() && playFromFile(ctx)) {
            return;
        }

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
            link = "ytsearch:" + link;

            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, link, true);
        }
        else {
            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, link, true);
        }

    }

    private boolean playFromFile(CommandContext ctx) {
        List<Message.Attachment> attachments = ctx.getMessage().getAttachments();

        Message.Attachment attachment = null;
        for(Message.Attachment att : attachments) {
            if(att.getFileExtension() != null && acceptedExtensions.contains(att.getFileExtension().toLowerCase())) {
                attachment = att;
                break;
            }
        }

        if(attachment == null) {
            return false;
        }

        LavalinkPlayerManager.getInstance().loadAndPlay(ctx, attachment.getUrl(), true);
        return true;
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
    public String getUsage(String prefix) {
        return prefix + "play <track_name/link>";
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

    @Override
    public int cooldownInSeconds() {
        return 5;
    }

    @Override
    public boolean mayAutoJoin() {
        return true;
    }
}