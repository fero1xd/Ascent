package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.util.List;

public class ForceSkip implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        forceSkip(false, null, ctx);

    }

    public static void forceSkip(boolean isInteraction, ButtonClickEvent event, CommandContext ctx) {
        TextChannel channel = !isInteraction ? ctx.getChannel() : event.getTextChannel();
        Member member = !isInteraction ? ctx.getMember() : event.getMember();
        Guild guild = !isInteraction ? ctx.getGuild() : event.getGuild();
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(guild);

        if(manager.audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);

            if(!isInteraction) {
                channel.sendMessageEmbeds(builder.build()).queue();
            }
            else {
                event.getMessage().delete().queue();
            }
            return;
        }


        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            if(!isInteraction) {
                channel.sendMessageEmbeds(Embeds.createBuilder(null, "You do not have enough permission", null, null, null).build()).queue();
            }
            else {
                event.replyEmbeds(Embeds.createBuilder(null, "You do not have enough permission", null, null, null).build())
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }


        if(manager.scheduler.votingGoingOn) {
            manager.scheduler.totalMembers.clear();
            manager.scheduler.votes.clear();
            manager.scheduler.votingGoingOn = false;
        }
        manager.scheduler.nextTrack();
        // EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);

        if(!isInteraction) {
            // channel.sendMessageEmbeds(builder.build()).queue();
            ctx.getMessage().addReaction("👍").queue();
        }
        else {
            event.getMessage().delete().queue();
        }
    }
    @Override
    public String getName() {
        return "forceskip";
    }

    @Override
    public List<String> getAliases() {
        return List.of("fs");
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getHelp() {
        return "Forcely skips a track";
    }

    @Override
    public int cooldownInSeconds() {
        return 3;
    }
}
