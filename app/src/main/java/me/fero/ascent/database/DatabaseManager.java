package me.fero.ascent.database;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface DatabaseManager {
    DatabaseManager INSTANCE = new MongoDbDataSource();

    String getPrefix(long guildId);
    void setPrefix(long guildId, String newPrefix);
}
