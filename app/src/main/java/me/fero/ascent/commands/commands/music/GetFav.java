package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.List;

public class GetFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        List<HashMap<String, String>> favourites = RedisDataStore.getInstance().getFavourites(ctx.getGuild().getIdLong(), ctx.getMember().getIdLong());
        if(favourites.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No favourite song till now ..", null, null, null).build()).queue();
            return;
        }

        EmbedBuilder builder = Embeds.createBuilder("Favourite tracks of " + ctx.getMember().getEffectiveName(),
                null, "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), null);

        int i = 1;
        for(HashMap<String, String> map : favourites) {
            String name = map.get("name");
            String link = map.get("link");

            builder.appendDescription("`" + i + ".` [" + name + "]"+ "(" +  link + ")" + "\n");
            i++;
        }
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "getfav";
    }


    @Override
    public String getHelp() {
        return "Gets the favourite tracks of the guild";
    }
}