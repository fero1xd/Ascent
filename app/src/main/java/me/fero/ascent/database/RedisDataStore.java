package me.fero.ascent.database;

import me.fero.ascent.Listener;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class RedisDataStore {
    private static RedisDataStore instance;
    private final Jedis redis;
    public static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);


    public RedisDataStore() {
        this.redis = new Jedis();
        LOGGER.info("Connection to redis success " + redis.ping());
    }


    public String getPrefix(Long idLong) {

        String prefix = this.redis.get(String.valueOf(idLong));
        if(prefix == null) {
            String prefix1 = DatabaseManager.INSTANCE.getPrefix(idLong);
            this.redis.set(String.valueOf(idLong), prefix1);
            return prefix1;
        }

        return prefix;

    }

    public void setPrefix(Long idLong, String prefix) {
        this.redis.set(String.valueOf(idLong), prefix);
    }

    public static RedisDataStore getInstance() {
        if(instance == null) {
            instance = new RedisDataStore();
        }

        return instance;
    }
}
