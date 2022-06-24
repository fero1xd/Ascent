package me.fero.ascent.commands.commands.general;

import me.duncte123.botcommons.web.WebUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

public class Quote extends BaseCommand {

    public Quote() {
        this.name = "quote";
        this.help = "Sends a random quote";
        this.cooldownInSeconds = 10;
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        WebUtils.ins.getJSONObject("https://api.kanye.rest/").async((json) -> {
                if(!json.has("quote")) {
                    channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot send a quote right now!", null, null, null).build()).queue();
                    return;
                }

                channel.sendMessageEmbeds(Embeds.createBuilder("Quote", json.get("quote").asText(), null, null, null).build()).queue();
            }, (e) -> {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Cannot send a quote right now!", null, null, null).build()).queue();

            });
    }
}
