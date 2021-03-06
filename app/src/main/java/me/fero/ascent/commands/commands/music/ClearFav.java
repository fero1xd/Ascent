package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.entities.Favourites;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ClearFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long memberId = ctx.getMember().getIdLong();

        Favourites favourites = RedisDataStore.getInstance().getFavourites(guildId, memberId);

        if(favourites.getFavourites().isEmpty()) {
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
