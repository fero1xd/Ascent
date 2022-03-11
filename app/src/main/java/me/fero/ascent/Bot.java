
package me.fero.ascent;


import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Waiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {
    private Bot() throws LoginException {
        System.setProperty("http.agent", "Chrome");
        SpotifyAudioSourceManager instance = SpotifyAudioSourceManager.INSTANCE;
        DatabaseManager instance1 = DatabaseManager.INSTANCE;
        RedisDataStore instance2 = RedisDataStore.getInstance();


        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(0x3883d9)
        );

        JDABuilder jda =  JDABuilder.createDefault(
                Config.get("TOKEN"),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
                );

        jda.disableCache(EnumSet.of(
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE
        ));
        jda.enableCache(CacheFlag.VOICE_STATE);

        jda.addEventListeners(new Listener(instance2), Waiter.instance.waiter);
        jda.addEventListeners(new SlashCommandListener());
        jda.build();
        YoutubeHttpContextFilter.setPAPISID(Config.get("papisid"));
        YoutubeHttpContextFilter.setPSID(Config.get("psid"));

    }


    public static void main(String[] args) throws LoginException {
        new Bot();
    }


}
