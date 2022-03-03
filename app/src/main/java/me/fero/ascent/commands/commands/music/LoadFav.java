package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoadFav implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        long guildId = ctx.getGuild().getIdLong();
        long userId = ctx.getMember().getIdLong();

        ArrayList<HashMap<String, String>> favourites = RedisDataStore.getInstance().getFavourites(guildId, userId);
        if(favourites.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Your list is empty...", null, null, null).build()).queue();
            return;
        }

        ArrayList<String> urls = new ArrayList<>();
        for(HashMap<String, String> entry : favourites) {
            urls.add(entry.get("link"));
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Loading " + ctx.getMember().getEffectiveName() + "'s favourite tracks", null, null, null).build()).queue();
        for(String url : urls) {
            PlayerManager.getInstance().audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    track.setUserData(ctx.getAuthor().getIdLong());

                    musicManager.scheduler.queue(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException exception) {

                }
            });
        }

    }

    @Override
    public String getName() {
        return "loadfav";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public List<String> getAliases() {
        return List.of("lfav");
    }

    @Override
    public String getHelp() {
        return "Loads the favourite songs of the user";
    }
}
