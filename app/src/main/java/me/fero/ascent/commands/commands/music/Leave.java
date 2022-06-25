package me.fero.ascent.commands.commands.music;


import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkManager;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Leave implements ICommand {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        final Member member =  ctx.getMember();

        final GuildVoiceState memberVoiceState = member.getVoiceState();

        Guild guild = ctx.getGuild();

//        LavalinkPlayerManager.getInstance().removeGuildMusicManager(guild);
        LavalinkManager.INS.closeConnection(guild);


        String desc = "Disconnected from "+ memberVoiceState.getChannel().getAsMention();
        EmbedBuilder builder = Embeds.createBuilder("Disconnected", desc, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getHelp() {
        return "Leaves the current vc";
    }

    @Override
    public List<String> getAliases() {
        return List.of("dc", "disconnect");
    }

    @Override
    public String getType() {
        return "music";
    }

}
