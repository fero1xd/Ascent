package me.fero.ascent.commands.commands.general;

import me.fero.ascent.Config;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class Vote extends BaseCommand {

    public Vote() {
        this.name = "vote";
        this.help = "Vote me on top.gg";
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        EmbedBuilder builder = Embeds.createBuilder("Vote me!", "We are accepting vote anytime.",
                "Requested by "
                        + ctx.getMember().getEffectiveName(),
                ctx.getMember().getEffectiveAvatarUrl(),
                null).addField("Link", "[Click me to vote](" + AscentConfig.get("vote_url") + ")", false).setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());


        channel.sendMessageEmbeds(builder.build()).queue();
    }
}
