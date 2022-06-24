package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.List;

public class Resume implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        resume(false, null, ctx);
    }

    public static void resume(boolean isInteraction, ButtonClickEvent event, CommandContext ctx) {
        TextChannel channel = !isInteraction ? ctx.getChannel() : event.getTextChannel();
        Member member = !isInteraction ? ctx.getMember() : event.getMember();
        Guild guild = !isInteraction ? ctx.getGuild() : event.getGuild();


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        AudioPlayer audioPlayer = musicManager.audioPlayer;

        if(audioPlayer.getPlayingTrack() == null) {
            if(!isInteraction) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            }
            else {
                event.replyEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).setEphemeral(true).queue();
            }
            return;
        }

        AudioPlayer player = musicManager.scheduler.player;
        if(!player.isPaused()) {
            if(!isInteraction) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Player is already playing", null, null, null).build()).queue();
                return;
            }

        }

        if(!isInteraction) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Player resumed", null, null, null).build()).queue();
        }
        else {
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder b = new EmbedBuilder(embed);
            b.setFooter("Resumed by " + member.getEffectiveName(), member.getEffectiveAvatarUrl());
            List<Button> controls = Embeds.getControls(true);
            // event.getMessage().editMessageEmbeds(b.build()).setActionRow(controls).queue();

            event.editMessageEmbeds(b.build()).setActionRow(controls).queue();
        }

        player.setPaused(false);
    }


    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getHelp() {
        return "Resumes the current track.";
    }

    @Override
    public String getType() {
        return "music";
    }

}
