package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.List;

public class Loop implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        loop(false, null, ctx);
    }


    public static void loop(boolean isInteraction, ButtonClickEvent event, CommandContext ctx) {
        Guild guild = !isInteraction ? ctx.getGuild() : event.getGuild();
        Member member = !isInteraction ? ctx.getMember() : event.getMember();
        TextChannel channel = !isInteraction ? ctx.getChannel() : event.getTextChannel();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        List<String> args = !isInteraction ? ctx.getArgs() : List.of();


        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setFooter("Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl());

        if(!isInteraction && !args.isEmpty() && args.get(0).equalsIgnoreCase("status")) {
            builder.setDescription("<:loop_ascent:989181126096601118> Loop is " + (musicManager.scheduler.isRepeating ? "Enabled" : "Disabled"));
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        final boolean newRepeating = !musicManager.scheduler.isRepeating;
        musicManager.scheduler.isRepeating = newRepeating;

        builder.setDescription("<:loop_ascent:989181126096601118> Loop is " + (newRepeating ? "Enabled" : "Disabled"));

        if(!isInteraction) {
            channel.sendMessageEmbeds(builder.build()).queue();
        }
        else {
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
        }
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
