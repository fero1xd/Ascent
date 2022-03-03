package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClearFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long memberId = ctx.getMember().getIdLong();

        ArrayList<HashMap<String, String>> favourites = RedisDataStore.getInstance().getFavourites(guildId, memberId);
        if(favourites.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Your list is empty...", null, null, null).build()).queue();
            return;
        }

        RedisDataStore.getInstance().clearFavourites(guildId, memberId);
        DatabaseManager.INSTANCE.clearFavourites(guildId, memberId);
        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Cleared your list", null, null, null).build()).queue();
    }

    @Override
    public String getName() {
        return "clearfav";
    }


    @Override
    public List<String> getAliases() {
        return List.of("clfav");
    }

    @Override
    public String getHelp() {
        return "Clears favourite list of a member";
    }
}
