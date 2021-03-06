package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.entities.Favourites;
import me.fero.ascent.entities.SavableTrack;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.bson.types.ObjectId;


import java.util.List;

public class Favourite implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        addToFavourite(false, null, ctx);
    }

    public static void addToFavourite(boolean isInteraction, ButtonClickEvent event, CommandContext ctx) {
        Guild guild = !isInteraction ? ctx.getGuild() : event.getGuild();
        long guildId = !isInteraction ? ctx.getGuild().getIdLong() : event.getGuild().getIdLong();
        long userId =  !isInteraction ? ctx.getMember().getIdLong() : event.getMember().getIdLong();

        TextChannel channel = !isInteraction ? ctx.getChannel() : null;

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(guild);


        if(musicManager.player.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            if (!isInteraction) {
                channel.sendMessageEmbeds(builder.build()).queue();
            } else {
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
            return;
        }

        AudioTrack playingTrack = musicManager.player.getPlayingTrack();

        Favourites favourites = RedisDataStore.getInstance().getFavourites(guildId, userId);

        for(SavableTrack entry : favourites.getFavourites()) {
            if(entry.getIdentifier().equals(playingTrack.getIdentifier())) {
                try {
                    RedisDataStore.getInstance().removeFavourite(guildId, userId, entry.getId());
                    DatabaseManager.INSTANCE.removeFavourite(guildId, userId, entry.getId());
                    if(!isInteraction) {
                        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Removed **" + playingTrack.getInfo().title + "** from your favourites", null, null, null).build()).queue();
                    }
                    else {
                        event.replyEmbeds(Embeds.createBuilder(null, "Removed **" + playingTrack.getInfo().title + "** from your favourites", null, null, null).build()).setEphemeral(true).queue();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if(!isInteraction) {
                        channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Error removing the track", null, null, null).build()).queue();
                    }
                    else {
                        event.replyEmbeds(Embeds.createBuilder("Error!", "Error removing the track", null, null, null).build()).setEphemeral(true).queue();
                    }
                }
                return;
            }
        }

        ObjectId objectId = new ObjectId();
        RedisDataStore.getInstance().addFavourite(guildId, userId, playingTrack, guild, String.valueOf(objectId));
        DatabaseManager.INSTANCE.addFavourite(guildId, userId, playingTrack, guild, String.valueOf(objectId));

        if(!isInteraction) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Added **" + playingTrack.getInfo().title + "** to your favourites", null, null, null).build()).queue();

        }else {
            event.replyEmbeds(Embeds.createBuilder(null, "Added **" + playingTrack.getInfo().title + "** to your favourites", null, null, null).build())
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "favourite";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("fav");
    }

    @Override
    public String getHelp() {
        return "Adds the current playing track to you favourites list";
    }

    @Override
    public int cooldownInSeconds() {
        return 6;
    }
}
