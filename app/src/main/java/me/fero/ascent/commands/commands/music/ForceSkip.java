package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ForceSkip implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        if(!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "You do not have enough permission", null, null, null).build()).queue();
            return;
        }

        if(manager.audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }


        if(manager.scheduler.votingGoingOn) {
            manager.scheduler.totalMembers.clear();
            manager.scheduler.votes.clear();
            manager.scheduler.votingGoingOn = false;
        }
        manager.scheduler.nextTrack();
        EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();

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
