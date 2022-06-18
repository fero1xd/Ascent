package me.fero.ascent.commands.commands;

import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.List;

public class DevInfo extends BaseCommand {
    public DevInfo() {
        this.name = "devinfo";
        this.aliases = List.of("di");
        this.help = "Gets you the details of my creator";
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        EmbedBuilder builder = Embeds.createBuilder("feroxd", "Hey there from my side !", "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), Color.RED);
        builder.addField("Name:", Config.get("dev_name"), true);
        builder.addField("ID:", Config.get("owner_id"), true);
        builder.setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());

        channel.sendMessageEmbeds(builder.build()).setActionRow(
                Button.link(Config.get("github_url"), "Github")
        ).queue();
    }
}
