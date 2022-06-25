package me.fero.ascent.commands.commands.music;

import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
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


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(guild);
        LavalinkPlayer audioPlayer = musicManager.player;

        if(audioPlayer.getPlayingTrack() == null) {
            if(!isInteraction) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).queue();
            }
            else {
                event.replyEmbeds(Embeds.createBuilder("Error!", "There is no track currently playing", null, null, null).build()).setEphemeral(true).queue();
            }
            return;
        }


        if(!audioPlayer.isPaused()) {
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
            event.editMessageEmbeds(b.build()).setActionRow(controls).queue();
        }

        audioPlayer.setPaused(false);
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
