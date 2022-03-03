package me.fero.ascent.commands.commands;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.Button;


public class Invite implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        String invite = Config.get("invite_url");

        EmbedBuilder builder = Embeds.createBuilder("Invite me!", null,
                        null,
                       null,
                        null);

        ctx.getChannel().sendMessageEmbeds(builder.build()).setActionRow(
                Button.link(invite, "Invite")
        ).queue();

    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getHelp() {
        return "Gets the invite link for the bot";
    }
}
