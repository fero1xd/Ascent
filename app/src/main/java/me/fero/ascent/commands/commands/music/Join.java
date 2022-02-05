package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class Join implements ICommand {
    @Override
    public void handle(CommandContext ctx) {

        final TextChannel channel = ctx.getChannel();

        final Member member =  ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        AudioManager audioManager = ctx.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        final VoiceChannel memberChannel = memberVoiceState.getChannel();

        // Embeds
        String title = "Connected";
        String description = "Connected successfully to " + memberChannel.getAsMention();
        String footer = "Requested by " + member.getEffectiveName();
        String footerUrl = member.getEffectiveAvatarUrl();
        EmbedBuilder builder = Embeds.createBuilder(title, description, footer, footerUrl, null);

        audioManager.openAudioConnection(memberChannel);

        channel.sendMessageEmbeds(builder.build()).queue();


    }


    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getHelp() {
        return "Makes the bot join your voice channel";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("j");
    }
}
