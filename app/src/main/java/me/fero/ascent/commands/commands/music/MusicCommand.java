package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class MusicCommand  {
    @SuppressWarnings("ConstantConditions")
    public static void handleMusicCommands(CommandContext ctx, ICommand cmd) {
        final TextChannel channel = ctx.getChannel();


        final Member selfMember = ctx.getSelfMember();

        GuildVoiceState selfVoiceState = selfMember.getVoiceState();
        final Member member =  ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inVoiceChannel()) {
            channel.sendMessageEmbeds(Embeds.notConnectedToVcEmbed(member).build()).queue();
            return;
        }

        AudioManager audioManager = ctx.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        if(!selfVoiceState.inVoiceChannel()) {
            if(cmd.getName().equalsIgnoreCase("leave")) {
                EmbedBuilder builder = Embeds.alreadyConnectedToVcEmbed(member);
                builder.setDescription("I am not connected to a voice channel");
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }
            // check if its a auto join command
            else if(cmd.getName().equalsIgnoreCase("join")
                    ||
                    cmd.getName().equalsIgnoreCase("play")
                    ||
                    cmd.getName().equalsIgnoreCase("search")
                    ||
                    cmd.getName().equalsIgnoreCase("scplay"))
            {
                if(!selfMember.hasPermission(Permission.VOICE_CONNECT))
                {
                    channel.sendMessageEmbeds(Embeds.notEnoughPermsEmbed(member).build()).queue();
                    return;
                }

                final VoiceChannel memberChannel = memberVoiceState.getChannel();
                audioManager.openAudioConnection(memberChannel);

                musicManager.scheduler.cachedChannel = ctx.getChannel();
                cmd.handle(ctx);
            }

            else {
                EmbedBuilder builder = Embeds.notInSameVcEmbed(member);
                builder.setDescription("Bot must be present in a VoiceChannel to use this Command");
                channel.sendMessageEmbeds(builder.build()).queue();
            }
            return;

        }

        if (!memberVoiceState.getChannel().getId().equals(selfVoiceState.getChannel().getId())) {
            channel.sendMessageEmbeds(Embeds.notInSameVcEmbed(member).setDescription("Already connected to a different channel").build()).queue();
            return;
        }
        else {
            if(cmd.getName().equalsIgnoreCase("join")) {
                channel.sendMessageEmbeds(Embeds.alreadyConnectedToVcEmbed(member).build()).queue();
                return;
            }
        }


        musicManager.scheduler.cachedChannel = ctx.getChannel();

        cmd.handle(ctx);

    }
}
