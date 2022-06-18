package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Move implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        if(args.isEmpty() || args.size() < 2) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Please specify a track index from the queue", null, null, null).build()).queue();
            return;

        }

        List<AudioTrack> queue = PlayerManager.getInstance().getMusicManager(ctx.getGuild()).scheduler.queue;

        if(queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();
            return;
        }

        int i1 = -1;
        int i2 = -1;

        try {
            i1 = Integer.parseInt(args.get(0)) - 1;
            i2 = Integer.parseInt(args.get(1)) - 1;
        }catch (NumberFormatException e) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        if(i1 == i2 ) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Both index are same", null, null, null).build()).queue();
            return;
        }

        if(i1 < 0 || i2 < 0) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        if(i1 > queue.size() || i2 > queue.size()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        AudioTrack trackToMove = queue.get(i1);
        AudioTrack trackToSwitchWith = queue.get(i2);



        if(trackToMove == null || trackToSwitchWith == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong input", null, null, null).build()).queue();
            return;
        }

        queue.set(i1, trackToSwitchWith);
        queue.set(i2, trackToMove);
        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Queue Updated ✅", null, null, null).build()).queue();
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String getHelp() {
        return "Moves the track in the queue to the given index";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage(String prefix) {
        return "move <index1> <index2>";
    }
}
