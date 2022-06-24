package me.fero.ascent.commands.commands.music;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.entities.Favourites;
import me.fero.ascent.entities.SavableTrack;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        Favourites favourites = RedisDataStore.getInstance().getFavourites(ctx.getGuild().getIdLong(), ctx.getMember().getIdLong());
        if(favourites.getFavourites().isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No favourite song till now ..", null, null, null).build()).queue();
            return;
        }

        EmbedBuilder builder = Embeds.createBuilder("Favourite tracks of " + ctx.getMember().getEffectiveName(),
                null, "Requested by " + ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(), null);

        int i = 1;
        for(SavableTrack track : favourites.getFavourites()) {
            String name = track.getName();
            String link = track.getLink();

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
