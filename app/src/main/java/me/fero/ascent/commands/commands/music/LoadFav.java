package me.fero.ascent.commands.commands.music;


import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.entities.Favourites;
import me.fero.ascent.entities.SavableTrack;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class LoadFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long userId = ctx.getMember().getIdLong();

        Favourites favourites = RedisDataStore.getInstance().getFavourites(guildId, userId);
        if(favourites.getFavourites().isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Your list is empty...", null, null, null).build()).queue();
            return;
        }

        ArrayList<String> urls = new ArrayList<>();
        for(SavableTrack track : favourites.getFavourites()) {
            urls.add(track.getLink());
        }

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Loading " + ctx.getMember().getEffectiveName() + "'s favourite tracks", null, null, null).build()).queue();

        for(String url : urls) {
            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, url, false);
        }
    }

    @Override
    public String getName() {
        return "loadfav";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lfav");
    }

    @Override
    public String getHelp() {
        return "Loads the favourite songs of the user";
    }
}
