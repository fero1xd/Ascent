package me.fero.ascent.commands.commands;

import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class Vote implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        EmbedBuilder builder = Embeds.createBuilder("Vote me!", "We are accepting vote anytime.",
                "Requested by "
                        + ctx.getMember().getEffectiveName(),
                ctx.getMember().getEffectiveAvatarUrl(),
                null).addField("Link", "[Click me to vote](" + Config.get("vote_url") + ")", false).setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());


        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "vote";
    }

    @Override
    public String getHelp() {
        return "Vote me on top.gg";
    }
}
