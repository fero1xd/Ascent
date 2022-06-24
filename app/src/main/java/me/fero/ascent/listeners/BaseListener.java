package me.fero.ascent.listeners;

import me.fero.ascent.database.RedisDataStore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class BaseListener extends ListenerAdapter {
    protected JDA jda;
    protected final RedisDataStore redis = RedisDataStore.getInstance();
}
