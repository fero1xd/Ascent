package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.List;

public class Pause implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
       pause(false, null, ctx);
    }

    public static void pause(boolean isInteraction, ButtonClickEvent event, CommandContext ctx) {
        TextChannel channel = !isInteraction ? ctx.getChannel() : event.getTextChannel();
        Member member = !isInteraction ? ctx.getMember() : event.getMember();
        Guild guild = !isInteraction ? ctx.getGuild() : event.getGuild();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);

        AudioPlayer audioPlayer = musicManager.audioPlayer;
        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);
            if(!isInteraction) {
                channel.sendMessageEmbeds(builder.build()).queue();
            }
            else {
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
            return;
        }

        AudioPlayer player = musicManager.scheduler.player;
        if(player.isPaused()) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Player is already paused", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);

            if(!isInteraction) {
                channel.sendMessageEmbeds(builder.build()).queue();
                return;
            }
        }

        EmbedBuilder builder = Embeds.createBuilder(null, "Player paused", "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);

        if(!isInteraction) {
            channel.sendMessageEmbeds(builder.build()).queue();
        }
        else {
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder b = new EmbedBuilder(embed);
            b.setFooter("Paused by " + member.getEffectiveName(), member.getEffectiveAvatarUrl());
            List<Button> controls = Embeds.getControls(false);
            //event.getMessage().editMessageEmbeds(b.build()).setActionRow(controls).queue();

            event.editMessageEmbeds(b.build()).setActionRow(controls).queue();
        }

        player.setPaused(true);
    }


    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getHelp() {
        return "Pauses the current song";
    }


    @Override
    public String getType() {
        return "music";
    }
}
