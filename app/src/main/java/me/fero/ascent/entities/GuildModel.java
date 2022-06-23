package me.fero.ascent.entities;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;


public class GuildModel implements Serializable {
    private final Long guildId;
    private String prefix;
    private ArrayList<Favourites> favourites;

    private HashSet<String> ignoredChannelsId;

    public GuildModel(Long guildId, String prefix, ArrayList<Favourites> favourites, HashSet<String> ignoredChannelsId) {
        this.guildId = guildId;
        this.prefix = prefix;
        this.favourites = favourites;
        this.ignoredChannelsId = ignoredChannelsId;
    }

    public String getPrefix(){
        return this.prefix;
    }

    @Nullable
    public Favourites getFavouritesOfUser(long userId) {
        for(Favourites fav : this.favourites) {
            if(fav.getUserId() == userId) {
                return fav;
            }
        }

        return null;
    }

    public void setPrefix(String newPrefix) {
        this.prefix = newPrefix;
    }

    public void addFavourites(SavableTrack newTrack, Long userId) {
        Favourites favouritesOfUser = this.getFavouritesOfUser(userId);

        if(favouritesOfUser != null) {
            favouritesOfUser.addFavourite(newTrack);
        }
    }

    public void addNewUserFavourites(Favourites fav) {
        Favourites favouritesOfUser = this.getFavouritesOfUser(fav.getUserId());
        if(favouritesOfUser == null) {
            this.favourites.add(fav);
        }
    }

    public void clearFavouritesOfUser(Long userId) {
        Favourites favouritesOfUser = this.getFavouritesOfUser(userId);
        if(favouritesOfUser != null) {
            favouritesOfUser.clearFavourites();
        }
    }

    public void removeFavourite(Long userId, String id) {
        Favourites favouritesOfUser = this.getFavouritesOfUser(userId);

        if(favouritesOfUser != null) {
            favouritesOfUser.removeFavourite(id);
        }
    }

    public HashSet<String> getIgnoredChannelsIds() { return this.ignoredChannelsId; }

    public void ignoreChannel(String key) {
        this.ignoredChannelsId.add(key);
    }
    public void unIgnoreChannel(String key) {
        this.ignoredChannelsId.remove(key);
    }

    public void setIgnoredChannelsIds(HashSet<String> set) { this.ignoredChannelsId = set; }
}
