package me.fero.ascent.listeners;

import me.fero.ascent.commands.commands.music.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;

public class ButtonListener extends BaseListener {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String componentId = event.getComponentId();

        // Vc check
        Member member = event.getMember();
        int code = MusicCommand.checkVc(member, event.getGuild().getSelfMember());

        if(code != 0) {
//            if(code == -1) {
//                event.replyEmbeds(Embeds.notConnectedToVcEmbed(member).build()).setEphemeral(true).queue();
//            }
//            else if (code == -2) {
//                EmbedBuilder builder = Embeds.notInSameVcEmbed(member);
//                builder.setDescription("Bot must be present in a VoiceChannel to use this Command");
//                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
//            }
//            else if (code == -3) {
//                event.replyEmbeds(Embeds.notInSameVcEmbed(member).setDescription("Already connected to a different channel").build()).setEphemeral(true).queue();
//            }

            // Will not respond
            return;
        }

        switch (componentId) {
            case "addToFavourite":
                Favourite.addToFavourite(true, event, null);
                break;
            case "pause":
                Pause.pause(true, event, null);
                break;
            case "resume":
                Resume.resume(true, event, null);
                break;
            case "skip":
                ForceSkip.forceSkip(true, event, null);
                break;
            case "loop":
                Loop.loop(true, event, null);
                break;
        }
    }
}
