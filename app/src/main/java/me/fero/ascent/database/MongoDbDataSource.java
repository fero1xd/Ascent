package me.fero.ascent.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import me.fero.ascent.Config;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;

import java.util.ArrayList;



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

}
