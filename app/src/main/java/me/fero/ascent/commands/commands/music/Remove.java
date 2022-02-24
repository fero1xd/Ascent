package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Remove implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        Guild guild = ctx.getGuild();
        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        List<AudioTrack> queue = musicManager.scheduler.queue;
        if(queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();
            return;
        }

        List<String> args = ctx.getArgs();
        if(args.isEmpty()) {

            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please specify a track index from the queue", null, null, null).build()).queue();
            return;
        }

        int index = -1;

        try {
            index = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please specify a track index from the queue", null, null, null).build()).queue();
            return;
        }

        if(index <= 0) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please specify a track index from the queue", null, null, null).build()).queue();
            return;
        }

        try {

            if(DatabaseManager.INSTANCE.isUsingFairMode(guild.getIdLong())) {

                Object userData = musicManager.scheduler.queue.get(index - 1).getUserData();
                String ownerId = userData.toString();
                long ownerIdLong = Long.parseLong(ownerId);

                Member owner = null;
                try {
                    owner = guild.getMemberById(ownerIdLong);
                } catch (Exception e )  {
                    e.printStackTrace();
                }

                if(owner == null) {
                    musicManager.scheduler.queue.remove(index - 1);

                    channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track removed successfully", null, null, null).build()).queue();
                    return;
                }

                List<Member> members = ctx.getSelfMember().getVoiceState().getChannel().getMembers();
                if(!members.contains(owner)) {
                    musicManager.scheduler.queue.remove(index - 1);

                    channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track removed successfully", null, null, null).build()).queue();
                    return;
                }

                if(ownerIdLong != ctx.getMember().getIdLong()) {
                    EmbedBuilder builder = Embeds.createBuilder(null, "You did not added this song to the queue so you cannot remove the song", null, null, null);
                    channel.sendMessageEmbeds(builder.build()).queue();
                    return;
                }
            }

            musicManager.scheduler.queue.remove(index - 1);
        } catch (Exception e) {
            if(e instanceof IndexOutOfBoundsException) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please specify a track index from the queue", null, null, null).build()).queue();
            }
            return;
        }

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track removed successfully", null, null, null).build()).queue();

    }
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getHelp() {
        return "Removes specified track from queue (Will not remove current track)";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage() {
        return "remove <track_index>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rm");
    }
}
