package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.net.URL;
import java.util.List;


public class Play implements ICommand {
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
            link = "ytsearch:" + link;

            PlayerManager.getInstance().loadAndPlay(ctx, link, false, null);
        }
        else {
//            if(link.toLowerCase().startsWith("https://open.spotify.com/playlist/") || link.toLowerCase().startsWith("https://open.spotify.com/track/") || link.toLowerCase().startsWith("https://open.spotify.com/album/")) {
//                WebUtils.ins.getJSONObject("http://"+ Config.get("ip") + ":6000/api?url=" + link).async((json) -> {
//                    if(json.has("error")) {
//                        channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot get a spotfiy track or playlist", null, null, null).build()).queue();
//                        return;
//                    }
//
//                    if(!json.has("url") && !json.has("songs")) {
//                        channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot get a spotfiy track or playlist", null, null, null).build()).queue();
//                        return;
//                    }
//
//                    if(json.has("songs")) {
//                        try {
//                            ObjectMapper objectMapper = new ObjectMapper();
//
//                            List<String> urls = objectMapper.readValue(json.get("songs").toString(), List.class);
//                            PlayerManager instance = PlayerManager.getInstance();
//                            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Spotify Playlist Loaded : Adding " + urls.size() + " Tracks to the queue", null, null, null).build()).queue();
//
//                            instance.queueMultipleUrl(ctx, urls);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        return;
//                    }
//                    PlayerManager.getInstance().loadAndPlay(ctx, json.get("url").asText(), false, null);
//                });
//            }
            PlayerManager.getInstance().loadAndPlay(ctx, link, false, null);

        }

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