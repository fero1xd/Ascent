package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Remove implements ICommand {
    private final EventWaiter waiter;

    public Remove(EventWaiter waiter){
        this.waiter = waiter;
    }
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

        String s = UUID.randomUUID().toString();

        SelectionMenu.Builder menu = SelectionMenu.create(s);
        menu.setPlaceholder("Select a track here");
        menu.setRequiredRange(1, 1);
        for(AudioTrack track : queue) {
            AudioTrackInfo info = track.getInfo();
            menu.addOption(info.title, String.valueOf(queue.indexOf(track)), info.author);
        }

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Select a track that you want to remove", null, null, null).build()).setActionRow(menu.build()).queue((message) -> {
            waiter.waitForEvent(
                    SelectionMenuEvent.class,
                    (e) -> {
                        if(!(e.getChannel() == channel && e.getMember() != null && !e.getMember().getUser().isBot() && e.getComponentId().equals(s))) {
                            return false;
                        }
                        if(e.getMember() != ctx.getMember()) {
                            e.reply("This menu is not for you").setEphemeral(true).queue();
                            return false;
                        }
                        return true;
                    },
                    (e) -> {
                        int index = Integer.parseInt(e.getValues().get(0));

                        message.delete().queue();
                        try {
                            queue.remove(index);
                            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track removed successfully", null, null, null).build()).queue();
                        } catch (Exception ex) {
                            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Error removing the track", null, null, null).build()).queue();
                        }
                    },
                    10, TimeUnit.SECONDS,
                    () -> {
                        message.delete().queue();
                    }
            );
        });
    }
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getHelp() {
        return "Removes specified track from queue";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rm");
    }

    @Override
    public boolean isDjNeeded() {
        return true;
    }
}
