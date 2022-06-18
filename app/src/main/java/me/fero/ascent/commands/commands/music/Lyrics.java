package me.fero.ascent.commands.commands.music;

import com.fasterxml.jackson.databind.JsonNode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import core.GLA;
import core.HttpManager;
import genius.SongSearch;
import me.duncte123.botcommons.web.WebUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class Lyrics implements ICommand {
    private HttpManager manager = new HttpManager();
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();
        GLA gla = new GLA();
        if(!args.isEmpty()) {
//            try {
//                SongSearch search = gla.search(String.join(" ", args));
//                if(search.getHits().isEmpty()) {
//                    channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot fetch lyrics", null, null, null).build()).queue();
//                    return;
//                }
//                String s = search.getHits().get(0).fetchLyrics().trim();
//                String s1 = s.replaceAll("\\[.*?\\]", "");
//
//                if(s1.length() >= 2000) {
//                    channel.sendMessageEmbeds(Embeds.createBuilder(null, "[Click here for lyrics](" + search.getHits().get(0).getUrl() + ")", null, null, null).build()).queue();
//                    return;
//                }
//                Member member = ctx.getMember();
//                channel.sendMessageEmbeds(Embeds.createBuilder(search.getHits().get(0).getTitle(), s1, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null).build()).queue();
//                return;
//            } catch (IOException e) {
//                e.printStackTrace();
//                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot fetch lyrics", null, null, null).build()).queue();
//                return;
//            }
            this.getLyrics(String.join(" ", args));
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;
        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        AudioTrack playingTrack = audioPlayer.getPlayingTrack();
        try {
            SongSearch search = gla.search(playingTrack.getInfo().title);
            LinkedList<SongSearch.Hit> hits = search.getHits();
            if(hits.isEmpty()) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot fetch lyrics", null, null, null).build()).queue();
                return;
            }
            String s = search.getHits().get(0).fetchLyrics().trim();
            String s1 = s.replaceAll("\\[.*?\\]", "");

            if(s1.length() >= 2000) {
                channel.sendMessageEmbeds(Embeds.createBuilder(null, "(Click here for lyrics)[" + search.getHits().get(0).getUrl() + "]", null, null, null).build()).queue();
                return;
            }
            Member member = ctx.getMember();

            channel.sendMessageEmbeds(Embeds.createBuilder(search.getHits().get(0).getTitle(), s1, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null).build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot fetch lyrics", null, null, null).build()).queue();
        }

    }


    @Override
    public String getName() {
        return "lyrics";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getHelp() {
        return "Gets the lyrics of the current playing track";
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "lyrics *<track name>";
    }

    private String getLyrics(String query) {

        try {
            query = URLEncoder.encode(query, "UTF-8");

            String token = "rVJPJjyoynYeQiyc1gsSJJPQ_2BhKNTPmNpu0Yc3D0OzWy-QMqaQUk4Ei7r7PySI";
            URI uri = new URI("https://api.genius.com/search?q=" + query);
            HttpURLConnection connection = this.manager.getConnection(uri.toURL());
            connection.setRequestProperty("Authorization", "Bearer " + token);
            String result = this.manager.executeGet(connection);

            JSONObject jsonObject = new JSONObject(result);

            int status = jsonObject.getJSONObject("meta").getInt("status");
            if(status != 200) {
                return null;
            }

            JSONObject hit = jsonObject.getJSONObject("response").getJSONArray("hits").getJSONObject(0).getJSONObject("result");
            System.out.println(hit.getString("full_title"));
//            WebUtils.ins.getJSONObject(uri).async((jRoot) -> {
//
//                JsonNode response = jRoot.get("response");

//                JsonNode result = response.get("hits").get(0).get("result");
//
//
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


    }


}
