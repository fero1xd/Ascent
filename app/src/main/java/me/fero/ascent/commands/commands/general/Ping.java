package me.fero.ascent.commands.commands.general;

import com.profesorfalken.jsensors.model.sensors.Temperature;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.objects.config.SystemConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

public class Ping extends BaseCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Shows the current ping from the bot to the discord server";
    }

    @Override
    public void execute(CommandContext ctx) {
        JDA jda = ctx.getJDA();
        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());
        List<Temperature> cpuTemperature = SystemConfig.getCpuTemperature();

        jda.getRestPing().queue(
                (__) -> {

                    List<Guild> guilds = ctx.getJDA().getGuilds();
                    long amount = 0;
                    for(Guild guild : guilds) {
                        amount += guild.getMemberCount();
                    }

                    EmbedBuilder builder = Embeds.createBuilder("Pong!", null, "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), null);
                    builder.addField("Current ping <a:onlineping:989099916439732244>", "`" + jda.getGatewayPing() + " ms" + "`", false);
                    builder.setThumbnail(ctx.getSelfMember().getEffectiveAvatarUrl());
                    builder.addField("Prefix", "`" + prefix + "`", false);
                    builder.addField("Serving", "`" + ctx.getJDA().getGuilds().size() + " Guilds" + "`", true);
                    builder.addField("Members", "`" + amount + "`", true);

                    if(cpuTemperature != null && !cpuTemperature.isEmpty()) {
                        builder.addField("Temperature", "`" + cpuTemperature.get(0).value + "`", false);
                    }


                    ctx.getChannel().sendMessageEmbeds(builder.build()).queue();
                }
        );
    }
}
