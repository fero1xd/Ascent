package me.fero.ascent.database;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.Listener;
import me.fero.ascent.entities.Favourites;
import me.fero.ascent.entities.GuildModel;

import me.fero.ascent.entities.SavableTrack;
import net.dv8tion.jda.api.entities.Guild;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RedisDataStore {
    private static RedisDataStore instance;
    private final RedissonClient redisson;
    public static final Logger LOGGER = LoggerFactory.getLogger(RedisDataStore.class);


    public RedisDataStore() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        this.redisson = Redisson.create(config);
    }


    public String getPrefix(Long idLong) {

        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(idLong));
        GuildModel guildModel = bucket.get();

        if(guildModel == null) {
            String prefix1 = DatabaseManager.INSTANCE.getPrefix(idLong);
            GuildModel model = new GuildModel(idLong, prefix1, new ArrayList<>(), null);
            bucket.set(model);
            return prefix1;
        }


        return guildModel.getPrefix();

    }

    public void setPrefix(Long idLong, String prefix) {

        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(idLong));
        GuildModel guildModel = bucket.get();

        guildModel.setPrefix(prefix);

        bucket.set(guildModel);
    }


    public Favourites getFavourites(Long guildId, Long userId) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();

        if(guildModel.getFavouritesOfUser(userId) == null){
            Favourites favourites = DatabaseManager.INSTANCE.getFavourites(guildId, userId);
            guildModel.addNewUserFavourites(favourites);
            bucket.set(guildModel);
            return favourites;
        }

        return guildModel.getFavouritesOfUser(userId);
    }

    public void addFavourite(Long guildId, Long userId, AudioTrack trackToAdd, Guild guild, String idToSet) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();
        Favourites favourites = guildModel.getFavouritesOfUser(userId);

        if(favourites == null) {
            Favourites favs = DatabaseManager.INSTANCE.getFavourites(guildId, userId);
            guildModel.addNewUserFavourites(favs);
        }

        SavableTrack info = new SavableTrack(idToSet,
                trackToAdd.getInfo().title,
                trackToAdd.getInfo().author,
                trackToAdd.getInfo().uri,
                trackToAdd.getInfo().identifier,
                String.valueOf((long) trackToAdd.getUserData())
        );

        guildModel.addFavourites(info, userId);
        bucket.set(guildModel);
    }

    public void clearFavourites(Long guildId, Long userId) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();
        guildModel.clearFavouritesOfUser(userId);
        bucket.set(guildModel);
    }

    public void removeFavourite(Long guildId, Long userId, String trackId) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();
        guildModel.removeFavourite(userId, trackId);
        bucket.set(guildModel);
    }

    public HashSet<String> getIgnoredChannels(Long guildId) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();

        if(guildModel.getIgnoredChannelsIds() == null) {
            HashSet<String> ignoredChannels = DatabaseManager.INSTANCE.getIgnoredChannels(guildId);
            guildModel.setIgnoredChannelsIds(ignoredChannels);
            bucket.set(guildModel);
            return ignoredChannels;
        }

        return guildModel.getIgnoredChannelsIds();
    }

    public void ignoreChannel(Long guildId, String key) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();
        guildModel.ignoreChannel(key);
        bucket.set(guildModel);
    }

    public void unIgnoreChannel(Long guildId, String key) {
        RBucket<GuildModel> bucket = this.redisson.getBucket(String.valueOf(guildId));
        GuildModel guildModel = bucket.get();
        guildModel.unIgnoreChannel(key);
        bucket.set(guildModel);
    }

    public static RedisDataStore getInstance() {
        if(instance == null) {
            instance = new RedisDataStore();
        }

        return instance;
    }
}
