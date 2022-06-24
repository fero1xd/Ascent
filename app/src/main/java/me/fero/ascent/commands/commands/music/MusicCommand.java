package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;

import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.lavaplayer.TrackScheduler;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;
import java.util.stream.Collectors;

public class MusicCommand {

    public static int checkVc(Member member, Member self) {
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if(!memberVoiceState.inVoiceChannel()) return -1;

        GuildVoiceState voiceState = self.getVoiceState();

        if(!voiceState.inVoiceChannel()) return -2;

        return memberVoiceState.getChannel().getId().equals(voiceState.getChannel().getId()) ? 0 : -3;
    }

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

        if(cmd.isDjNeeded()) {
            Role dj = member.getRoles().stream().filter((role) -> role.getName().equalsIgnoreCase("DJ")).findFirst().orElse(null);
            boolean canInteract = ctx.getGuild().getSelfMember().canInteract(member);
            List<Member> collect = memberVoiceState.getChannel().getMembers().stream().filter((mem) -> !mem.getUser().isBot() && mem != member).collect(Collectors.toList());

            if(dj==null && canInteract && !collect.isEmpty()) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "You do not have the DJ Role in the server", null, null, null).build()).queue();
                return;
            }
        }

        AudioManager audioManager = ctx.getGuild().getAudioManager();
        audioManager.setSelfDeafened(true);

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;

        if(!selfVoiceState.inVoiceChannel()) {
            if(cmd.getName().equalsIgnoreCase("leave")) {
                EmbedBuilder builder = Embeds.alreadyConnectedToVcEmbed(member);
                builder.setDescription("I am not connected to a voice channel");
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }
            // check if its a auto join command
            else if(cmd.mayAutoJoin())
            {
                final VoiceChannel memberChannel = memberVoiceState.getChannel();

                if(!selfMember.hasPermission(memberChannel, Permission.VOICE_CONNECT))
                {
                    channel.sendMessageEmbeds(Embeds.notEnoughPermsEmbed(member).build()).queue();
                    return;
                }

                if(memberChannel.getType() == ChannelType.STAGE) {
                    StageChannel stageChannelById = ctx.getGuild().getStageChannelById(memberChannel.getId());

                    if(!stageChannelById.isModerator(ctx.getSelfMember())) {
                        channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "I do not have `manage channel` permission on this stage", null, null, null).build()).queue();
                        return;
                    }

                    if(stageChannelById.getStageInstance() == null) {
                       stageChannelById.createStageInstance("Ascent Music").queue();
                    }
                    ctx.getGuild().requestToSpeak();
                }

                audioManager.openAudioConnection(memberChannel);
                scheduler.setBindedChannel(ctx.getChannel());
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

        scheduler.setBindedChannel(ctx.getChannel());
        cmd.handle(ctx);
    }
}
