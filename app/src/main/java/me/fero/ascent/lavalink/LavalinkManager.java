package me.fero.ascent.lavalink;

import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.Ascent;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.objects.config.AscentConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Base64;


public class LavalinkManager {
    public static LavalinkManager INS = new LavalinkManager();
    private JdaLavalink lavalink = null;
    private PlayerManager manager;
    private boolean enabledOverride = true;
    private AscentConfig.Lavalink config;

    private LavalinkManager() {}


    public void start(Ascent ascent) {
        this.manager = PlayerManager.getInstance();
        this.config = AscentConfig.getLavalinkNodes();

        if(!isEnabled()) {
            return;
        }

        String id = getIdFromToken(AscentConfig.get("token"));
        lavalink = new JdaLavalink(id, 1, integer -> ascent.getJDA());

        loadNodes();
    }

    public boolean isEnabled() {
        return this.enabledOverride && config.isEnabled;
    }

    public LavalinkPlayer createPlayer(long guildId) {
        if (!isEnabled()) {
            throw new IllegalStateException("Music is not enabled right now");
        }

        return lavalink.getLink(String.valueOf(guildId)).getPlayer();
    }

    public void openConnection(VoiceChannel channel) {
        if (isEnabled()) {
            final AudioManager audioManager = channel.getGuild().getAudioManager();

            // Turn on the deafen icon for the bot
            audioManager.setSelfDeafened(true);

            lavalink.getLink(channel.getGuild()).connect(channel);
        }
    }

    public void closeConnection(Guild guild) {
        closeConnection(guild.getId());
    }

    public void closeConnection(String guildId) {
        if (isEnabled()) {
            lavalink.getLink(guildId).destroy();
        }
    }

    public boolean isConnected(Guild guild) {
        return isConnected(guild.getId());
    }

    public boolean isConnected(String guildId) {
        if (!isEnabled()) {
            return false;
        }

        return lavalink.getLink(guildId).getState() == Link.State.CONNECTED;
    }

    @SuppressWarnings("ConstantConditions") // cache is enabled
    public VoiceChannel getConnectedChannel(@Nonnull Guild guild) {
        // NOTE: never use the local audio manager, since the audio connection may be remote
        // there is also no reason to look the channel up remotely from lavalink, if we have access to a real guild
        // object here, since we can use the voice state of ourselves (and lavalink 1.x is buggy in keeping up with the
        // current voice channel if the bot is moved around in the client)
        return guild.getSelfMember().getVoiceState().getChannel();
    }


    public void shutdown() {
        if (isEnabled()) {
            this.lavalink.shutdown();
        }
    }

    public JdaLavalink getLavalink() {
        return lavalink;
    }

    private void loadNodes() {
        final JdaLavalink lavalink = getLavalink();

        for (final AscentConfig.Lavalink.LavalinkNode node : config.nodes) {
            lavalink.addNode(URI.create(node.wsurl), node.pass);
        }
    }

    private String getIdFromToken(String token) {
        return new String(
                Base64.getDecoder().decode(
                        token.split("\\.")[0]
                )
        );
    }
}
