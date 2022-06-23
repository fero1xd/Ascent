package me.fero.ascent.database;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.entities.Favourites;
import net.dv8tion.jda.api.entities.Guild;


import java.util.HashSet;

public interface DatabaseManager {
    DatabaseManager INSTANCE = new MongoDbDataSource();

    String getPrefix(long guildId);
    void setPrefix(long guildId, String newPrefix);
    void addFavourite(long guildId, long userId, AudioTrack track, Guild guild, String idToSet);
    Favourites getFavourites(long guildId, long userId);
    void clearFavourites(long guildId, long userId);
    void removeFavourite(long guildId, long userId, String trackId);
    HashSet<String> getIgnoredChannels(long guildId);
    void ignoreChannel(long guildId, String key);
    void unIgnoreChannel(long guildId, String key);
}
