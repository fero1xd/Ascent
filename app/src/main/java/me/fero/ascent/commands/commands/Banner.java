package me.fero.ascent.commands.commands;

import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Banner implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        User author = ctx.getAuthor();
        try {
            URL url = new URL("https://discord.com/api/users/"+ author.getId());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            String auth = "Bot " + Config.get("TOKEN");
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "application/json");

            http.setRequestProperty("Authorization", auth);

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JSONObject jsonObject = new JSONObject(response.toString());
                if(!jsonObject.isNull("banner")) {
                    String banner = jsonObject.getString("banner");
                    String ext = banner.startsWith("a_") ? ".gif" : ".png";
                    String last_url = "https://cdn.discordapp.com/banners/" + author.getId() + "/" + banner + ext;
                    channel.sendMessageEmbeds(Embeds.createBuilder(null, author.getAsTag() + "'s banner", null, null, null).setImage(last_url).build()).queue();

                }
                else {
                    channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "No banner found", null, null, null).build()).queue();
                }
            }
            http.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "This command ran into some issue", null, null, null).build()).queue();
        }


    }

    @Override
    public String getName() {
        return "banner";
    }

    @Override
    public String getHelp() {
        return "Gets the banner of the user";
    }

    @Override
    public int cooldownInSeconds() {
        return 10;
    }
}
