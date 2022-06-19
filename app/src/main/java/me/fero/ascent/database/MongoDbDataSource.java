package me.fero.ascent.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.Config;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;


public class MongoDbDataSource implements DatabaseManager{

    private final Logger LOGGER = LoggerFactory.getLogger(MongoDbDataSource.class);
    private final MongoDatabase db;


    public MongoDbDataSource() {
        MongoClient client = MongoClients.create(Config.get("MONGO_URI"));
        this.db = client.getDatabase("ascent_bot");
        LOGGER.info("Connected to Mongo DB");
    }

    @Override
    public String getPrefix(long guildId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);

        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");

        Document cursor = guild_settings.find(whereQuery).first();

        if(cursor == null) {
            Document newDoc = new Document("_id", guildId);
            newDoc.append("prefix", Config.get("prefix"));
            newDoc.append("favourites", new BasicDBList());

            guild_settings.insertOne(newDoc);
            return Config.get("prefix");
        }

        return cursor.get("prefix").toString();
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {
        Bson filter = Filters.eq("_id", guildId);
        Bson updated = Updates.set("prefix", newPrefix);

        this.db.getCollection("guild_settings").updateOne(filter, updated);
    }

    @Override
    public void addFavourite(long guildId, long userId, AudioTrack track, Guild guild, String idToSet) {

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);


        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");

        Document cursor = guild_settings.find(whereQuery).first();

        HashMap<String, String> info = new HashMap<>();

        info.put("_id", idToSet);
        info.put("name", track.getInfo().title);
        info.put("artist", track.getInfo().author);
        info.put("link", track.getInfo().uri);
        info.put("identifier", track.getInfo().identifier);
        info.put("user", String.valueOf((long) track.getUserData()));
        Bson filter = Filters.eq("_id", guildId);

        List<Document> favourites = (List<Document>) cursor.get("favourites");
        if(favourites.isEmpty()) {
            HashMap<String, ArrayList<HashMap<String, String>>> map = new HashMap<>();
            ArrayList<HashMap<String, String>> arr = new ArrayList<>();
            arr.add(info);
            map.put(String.valueOf(userId), arr);
            Bson updated = Updates.push("favourites", map);
            guild_settings.updateOne(filter, updated);
            return;
        }


        boolean isThere = false;
        int index = -1;
        for(Document fav : favourites){
            if(fav.get(String.valueOf(userId)) != null) {
                index = favourites.indexOf(fav);
                isThere = true;
            }
        }

        // [
        //
        // {
        //      "132424": {}
        // }
        // {
        //      "132424": {}
        // }
        //      "132424": {}
        // ]

        if(!isThere) {
            HashMap<String, ArrayList<HashMap<String, String>>> map = new HashMap<>();
            ArrayList<HashMap<String, String >> arr = new ArrayList<>();
            arr.add(info);
            map.put(String.valueOf(userId), arr);
            Bson updated = Updates.push("favourites", map);
            guild_settings.updateOne(filter, updated);
            return;
        }
        Bson updated = Updates.push("favourites." + index + "." + userId, info);
        guild_settings.updateOne(filter, updated);
    }


    @Override
    public ArrayList<HashMap<String, String>> getFavourites(long guildId, long userId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);

        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");

        Document cursor = guild_settings.find(whereQuery).first();

        List<Document> favourites = (List<Document>) cursor.get("favourites");


        ArrayList<HashMap<String, String>> favs = new ArrayList<>();

        for(Document doc : favourites) {
            if(doc.get(String.valueOf(userId)) != null) {
                List<Document> favsOfUser =  (List<Document>) doc.get(String.valueOf(userId));
                for(Document entry : favsOfUser) {
                    HashMap<String, String> info = new HashMap<>();
                    info.put("_id", (String) entry.get("_id"));
                    info.put("name", (String) entry.get("name"));
                    info.put("artist", (String) entry.get("artist"));
                    info.put("link", (String) entry.get("link"));
                    info.put("identifier", (String) entry.get("identifier"));
                    if(entry.get("user") != null) {
                        info.put("user", (String) entry.get("user"));
                    }
                    favs.add(info);
                }
                break;
            }
        }
        return favs;
    }

    @Override
    public void clearFavourites(long guildId, long userId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);
        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");
        Document cursor = guild_settings.find(whereQuery).first();
        List<Document> favourites = (List<Document>) cursor.get("favourites");

        if(favourites.isEmpty()) return;
        boolean isThere = false;
        int index = -1;
        for(Document fav : favourites){
            if(fav.get(String.valueOf(userId)) != null) {
                index = favourites.indexOf(fav);
                isThere = true;
            }
        }


        if(!isThere) return;
        Bson filter = Filters.eq("_id", guildId);
        Bson updated = Updates.set("favourites." + index + "." + userId, new BasicDBList());
        guild_settings.updateOne(filter, updated);
    }

    @Override
    public void removeFavourite(long guildId, long userId, String trackId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);
        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");
        Document cursor = guild_settings.find(whereQuery).first();
        List<Document> favourites = (List<Document>) cursor.get("favourites");


        if(favourites.isEmpty()) return;
        int index = -1;
        for(Document fav : favourites){
            if(fav.get(String.valueOf(userId)) != null) {
                List<Document> favs = (List<Document>) fav.get(String.valueOf(userId));
                for(Document doc : favs) {
                    String id = (String) doc.get("_id");
                    if(id.equals(trackId)) {
                        favs = favs.stream().filter((item) -> !Objects.equals((String) item.get("_id"), trackId)).collect(Collectors.toList());
                        break;
                    }
                }
                index = favourites.indexOf(fav);
                Bson filter = Filters.eq("_id", guildId);
                Bson updated = Updates.set("favourites." + index + "." + userId, favs);
                guild_settings.updateOne(filter, updated);
                break;
            }
        }

    }

    @Override
    public HashSet<String> getIgnoredChannels(long guildId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);
        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");
        Document cursor = guild_settings.find(whereQuery).first();


        if(cursor.get("ignored_channels") == null) {
            Bson filter = Filters.eq("_id", guildId);
            Bson updated = Updates.set("ignored_channels", new BasicDBList());
            guild_settings.updateOne(filter, updated);
            return new HashSet<>();
        }

        List<String> igChannels = (List<String>) cursor.get("ignored_channels");

        return new HashSet<>(igChannels);
    }

    @Override
    public void ignoreChannel(long guildId, String key) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);
        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");
        Document cursor = guild_settings.find(whereQuery).first();

        List<String> igChannels = (List<String>) cursor.get("ignored_channels");

        HashSet<String> set = new HashSet<>(igChannels);

        if(set.add(key)) {
            igChannels.add(key);
        }

        Bson filter = Filters.eq("_id", guildId);
        Bson updated = Updates.set("ignored_channels", igChannels);

        guild_settings.updateOne(filter, updated);
    }

    @Override
    public void unIgnoreChannel(long guildId, String key) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", guildId);
        MongoCollection<Document> guild_settings = this.db.getCollection("guild_settings");
        Document cursor = guild_settings.find(whereQuery).first();

        List<String> igChannels = (List<String>) cursor.get("ignored_channels");

        igChannels.remove(key);
        Bson filter = Filters.eq("_id", guildId);
        Bson updated = Updates.set("ignored_channels", igChannels);

        guild_settings.updateOne(filter, updated);
    }
}
