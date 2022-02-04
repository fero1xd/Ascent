package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public class Ping implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        JDA jda = ctx.getJDA();
        jda.getRestPing().queue(
                (ping) -> {
                    ctx.getChannel().sendMessageEmbeds(
                            Embeds.createBuilder("Current ping", "Rest ping: " + ping +"\nWS ping: " + jda.getGatewayPing(), "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), null).build()
                    ).queue();
                }
        );
    }

    @Override
    public String getHelp() {
        return "Shows the current ping from the bot to the discord server";
    }

    @Override
    public String getName() {
        return "ping";
    }
}
