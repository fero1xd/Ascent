package me.fero.ascent.database;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface DatabaseManager {
    DatabaseManager INSTANCE = new MongoDbDataSource();

    String getPrefix(long guildId);
    void setPrefix(long guildId, String newPrefix);
    void addFavourite(long guildId, long userId, AudioTrack track, Guild guild, String idToSet);
    ArrayList<HashMap<String, String>> getFavourites(long guildId, long userId);
    void clearFavourites(long guildId, long userId);
    void removeFavourite(long guildId, long userId, String trackId);

}
