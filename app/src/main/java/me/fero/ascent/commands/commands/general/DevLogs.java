package me.fero.ascent.commands.commands.general;

import me.fero.ascent.Config;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.io.IO;
import me.fero.ascent.io.Response;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class DevLogs implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();


        new Thread(() -> {
            EmbedBuilder builder = Embeds.createBuilder("Last 10 commits of Ascent", null, "Requested by " + member.getEffectiveName(),
                    member.getEffectiveAvatarUrl(), null);

            Response response = IO.request("https://api.github.com/repos/fero1xd/Ascent/commits?per_page=10");

            try {
                JSONArray commits = (JSONArray) Config.parser.parse(response.getResponse());

                int i = 1;
                for(Object ob : commits) {
                     JSONObject item = (JSONObject) Config.parser.parse(ob.toString());

                    JSONObject commit = (JSONObject) Config.parser.parse(item.get("commit").toString());

                    builder.appendDescription(i + ". [" + commit.get("message").toString() + "](" + commit.get("url").toString() + ")\n");
                    i++;
                }

                channel.sendMessageEmbeds(builder.build()).queue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String getName() {
        return "devlogs";
    }

    @Override
    public String getHelp() {
        return "Shows the latest 10 commits of the bot";
    }

    @Override
    public int cooldownInSeconds() {
        return 60;
    }
}
