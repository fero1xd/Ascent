package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;


import java.util.ArrayList;
import java.util.List;


public class Queue implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        final List<AudioTrack> queue = musicManager.scheduler.queue;

        if (queue.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.queueIsEmptyEmbed().build()).queue();

            return;
        }

        final int trackCount = Math.min(queue.size(), 20);
        final List<AudioTrack> trackList = new ArrayList<>(queue);

        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setTitle("Queue for " + ctx.getGuild().getName() + " 📀");
        builder.setFooter("Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl());



        for (int i = 0; i <  trackCount; i++) {
            final AudioTrack track = trackList.get(i);
            final AudioTrackInfo info = track.getInfo();

            builder.appendDescription(i+1 + ". `" + info.title + "" + " by " + info.author + "`" + "\n");

        }

        if (trackList.size() > trackCount) {
            builder.appendDescription("And `" + String.valueOf(trackList.size() - trackCount) +"` more...");

        }

        channel.sendMessageEmbeds(builder.build()).queue();

    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getHelp() {
        return "Shows the queue upto 20 tracks";
    }


    @Override
    public List<String> getAliases() {
        return List.of("q");
    }

    @Override
    public String getType() {
        return "music";
    }

}
