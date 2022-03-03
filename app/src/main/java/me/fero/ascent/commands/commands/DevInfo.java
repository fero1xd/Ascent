package me.fero.ascent.commands.commands;

import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.List;

public class DevInfo implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        EmbedBuilder builder = Embeds.createBuilder("Feroxd", "Hey there from my side !", "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), Color.RED);
        builder.addField("Name:", Config.get("dev_name"), true);
        builder.addField("ID:", Config.get("owner_id"), true);
        builder.setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());

        channel.sendMessageEmbeds(builder.build()).setActionRow(
                Button.link(Config.get("github_url"), "Github")
        ).queue();
    }

    @Override
    public String getName() {
        return "devinfo";
    }

    @Override
    public List<String> getAliases() {
        return List.of("di");
    }

    @Override
    public String getHelp() {
        return "Gets you the details of my creator";
    }
}
