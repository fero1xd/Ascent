package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class SkipTo implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        List<AudioTrack> queue = musicManager.scheduler.queue;


        if(args.isEmpty()) {
            // TODO: ERROR
            return;
        }

        if(queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();
            return;
        }

        int i1 = -1;

        try {
            i1 = Integer.parseInt(args.get(0)) - 1;
        }catch (NumberFormatException e) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }


        if(i1 < 0) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        if(i1 > queue.size()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        musicManager.scheduler.player.startTrack(queue.get(i1), false);

        for(int i = 0; i < i1 + 1; i++) {
            queue.remove(i);
        }

    }

    @Override
    public String getName() {
        return "skipto";
    }

    @Override
    public String getHelp() {
        return "Skips the queue till the song index given";
    }

    @Override
    public String getType() {
        return "music";
    }
}
