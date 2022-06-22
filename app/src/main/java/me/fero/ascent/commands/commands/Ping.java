package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.Button;

public class Ping extends BaseCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Shows the current ping from the bot to the discord server";
    }

    @Override
    public void execute(CommandContext ctx) {
        JDA jda = ctx.getJDA();
        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());
        jda.getRestPing().queue(
                (__) -> {
                    EmbedBuilder builder = Embeds.createBuilder("Pong!", null, "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), null);
                    builder.addField("Current ping <a:onlineping:989099916439732244>", "`" + jda.getGatewayPing() + " ms" + "`", false);
                    builder.setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());
                    builder.addField("Prefix", "`" + prefix + "`", false);
                    builder.addField("Serving", "`" + ctx.getJDA().getGuilds().size() + " Guilds" + "`", false);
                    ctx.getChannel().sendMessageEmbeds(builder.build()).queue();
                }
        );
    }
}
