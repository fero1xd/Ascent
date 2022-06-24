package me.fero.ascent.commands.commands.general;

import me.fero.ascent.Config;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.Button;


public class Invite extends BaseCommand {

    public Invite() {
        this.name = "invite";
        this.help = "Gets the invite link for the bot";
    }

    @Override
    public void execute(CommandContext ctx) {
        String invite = AscentConfig.get("invite_url");

        EmbedBuilder builder = Embeds.createBuilder("Invite me!", null,
                        null,
                       null,
                        null);

        ctx.getChannel().sendMessageEmbeds(builder.build()).setActionRow(
                Button.link(invite, "Invite")
        ).queue();

    }
}
