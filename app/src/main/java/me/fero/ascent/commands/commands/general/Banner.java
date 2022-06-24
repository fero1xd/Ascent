package me.fero.ascent.commands.commands.general;

import me.fero.ascent.Config;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Banner extends BaseCommand {

    public Banner() {
        this.name = "banner";
        this.help = "Gets the banner of the user";
        this.cooldownInSeconds = 10;
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        User author = ctx.getAuthor();
        try {
            URL url = new URL("https://discord.com/api/users/"+ author.getId());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            String auth = "Bot " + AscentConfig.get("token");
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "application/json");

            http.setRequestProperty("Authorization", auth);

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
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
}