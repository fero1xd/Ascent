package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
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

public class Skip implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        Guild guild = ctx.getGuild();


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;
        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        if(DatabaseManager.INSTANCE.isUsingFairMode(guild.getIdLong())) {
            Object userData = audioPlayer.getPlayingTrack().getUserData();
            String ownerId = userData.toString();
            long ownerIdLong = Long.parseLong(ownerId);

            Member owner = null;
            try {
                owner = guild.getMemberById(ownerIdLong);
            } catch (Exception e )  {
                e.printStackTrace();
            }

            if(owner == null) {
                musicManager.scheduler.nextTrack();
                EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }

            List<Member> members = ctx.getSelfMember().getVoiceState().getChannel().getMembers();
            if(!members.contains(owner)) {
                musicManager.scheduler.nextTrack();
                EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }

            if(ownerIdLong != ctx.getMember().getIdLong()) {
                EmbedBuilder builder = Embeds.createBuilder(null, "You did not added this song to the queue so you cannot skip the song", null, null, null);
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }
        }


        musicManager.scheduler.nextTrack();
        EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp() {
        return "Skips the current song";
    }


    @Override
    public List<String> getAliases() {
        return List.of("s");
    }

    @Override
    public String getType() {
        return "music";
    }
}
