package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RemoveFav implements ICommand {
    private final EventWaiter waiter;

    public RemoveFav(EventWaiter waiter) {
        this.waiter = waiter;
    }
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long userId = ctx.getMember().getIdLong();

        ArrayList<HashMap<String, String>> favourites = RedisDataStore.getInstance().getFavourites(guildId, userId);
        if(favourites.isEmpty()){
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Your list is empty...", null, null, null).build()).queue();
            return;
        }

        String s = UUID.randomUUID().toString();
        SelectionMenu.Builder menu = SelectionMenu.create(s);
        menu.setPlaceholder("Select your track here");
        menu.setRequiredRange(1, 1);

        for(HashMap<String, String> entry : favourites) {
            menu.addOption(entry.get("name"), String.valueOf(favourites.indexOf(entry)), entry.get("artist"));
        }

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Choose a track to remove from your list", null, null, null).build()).setActionRow(menu.build()).queue((message) -> {
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
                            HashMap<String, String> track = favourites.get(index);
                            RedisDataStore.getInstance().removeFavourite(guildId, userId, track.get("_id"));
                            DatabaseManager.INSTANCE.removeFavourite(guildId, userId, track.get("_id"));
                            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Track removed successfully", null, null, null).build()).queue();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Error removing the track", null, null, null).build()).queue();
                        }

                    },
                    15, TimeUnit.SECONDS,
                    () -> {
                        message.delete().queue();
                    }
            );
        });
    }

    @Override
    public String getName() {
        return "rmfav";
    }

    @Override
    public String getHelp() {
        return "Removes a track from the list of the member";
    }
}