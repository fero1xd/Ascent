
package me.fero.ascent;


import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavalink.LavalinkManager;
import me.fero.ascent.listeners.BaseListener;
import me.fero.ascent.listeners.BotListener;
import me.fero.ascent.listeners.ButtonListener;
import me.fero.ascent.listeners.GuildListener;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.spotify.SpotifyAudioSource;
import me.fero.ascent.spotify.SpotifyAudioSourceManager;
import me.fero.ascent.utils.Waiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;


public class Ascent {

    private final JDA jda;
    private final SpotifyAudioSource sp;

    private Ascent() throws LoginException {
        System.setProperty("http.agent", "Chrome");
        this.sp = (SpotifyAudioSource) SpotifyAudioSourceManager.INSTANCE;

        DatabaseManager _db = DatabaseManager.INSTANCE;
        RedisDataStore.getInstance();


        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(0x3883d9)
        );

        LavalinkManager.INS.start(this);

        JDABuilder jda = JDABuilder.createDefault(
                AscentConfig.get("token"),
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

        jda.addEventListeners(
                new BaseListener(),
                new BotListener(),
                new GuildListener(),
                new ButtonListener(),
                Waiter.instance.waiter
        );

        if(LavalinkManager.INS.isEnabled()) {
            jda.setVoiceDispatchInterceptor(LavalinkManager.INS.getLavalink().getVoiceInterceptor());
        }

        this.jda = jda.build();

        YoutubeHttpContextFilter.setPAPISID(AscentConfig.get("papisid"));
        YoutubeHttpContextFilter.setPSID(AscentConfig.get("psid"));
    }

    public JDA getJDA() {
        return jda;
    }

    public static void main(String[] args) throws LoginException {
        new Ascent();
    }
}
