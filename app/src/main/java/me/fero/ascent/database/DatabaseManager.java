package me.fero.ascent.database;


public interface DatabaseManager {
    DatabaseManager INSTANCE = new MongoDbDataSource();

    String getPrefix(long guildId);
    void setPrefix(long guildId, String newPrefix);

    boolean isUsingFairMode(long guildId);
    void setFairMode(long guildId, boolean fairMode);
}
