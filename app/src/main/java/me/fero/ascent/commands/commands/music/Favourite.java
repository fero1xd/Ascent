package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Favourite implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long userId = ctx.getMember().getIdLong();


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        if(musicManager.scheduler.player.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        AudioTrack playingTrack = musicManager.scheduler.player.getPlayingTrack();

        ArrayList<HashMap<String, String>> favourites = RedisDataStore.getInstance().getFavourites(guildId, userId);
        for(HashMap<String, String> entry : favourites) {
            if(entry.get("identifier").equals(playingTrack.getIdentifier())) {
                channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "This track is already in your list", null, null, null).build()).queue();
                return;
            }
        }

        ObjectId objectId = new ObjectId();
        RedisDataStore.getInstance().addFavourite(guildId, userId, playingTrack, ctx.getGuild(), String.valueOf(objectId));
        DatabaseManager.INSTANCE.addFavourite(guildId, userId, playingTrack, ctx.getGuild(), String.valueOf(objectId));
        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Added this track to your favourites", null, null, null).build()).queue();
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
