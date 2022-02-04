package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Loop implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        final Member member =  ctx.getMember();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        List<String> args = ctx.getArgs();


        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setFooter("Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl());
        if(!args.isEmpty() && args.get(0).equalsIgnoreCase("status")) {

            builder.setDescription("➿ Loop is " + (musicManager.scheduler.isRepeating ? "Enabled" : "Disabled"));

            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        final boolean newRepeating = !musicManager.scheduler.isRepeating;
        musicManager.scheduler.isRepeating = newRepeating;
        builder.setDescription("➿ Loop is " + (newRepeating ? "Enabled" : "Disabled"));

        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String getHelp() {
        return "Loops the current song";
    }

    @Override
    public String getType() {
        return "music";
    }


}
