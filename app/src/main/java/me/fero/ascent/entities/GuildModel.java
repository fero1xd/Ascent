package me.fero.ascent.entities;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GuildModel implements Serializable {
    private final Long guildId;
    private String prefix;

    private ArrayList<HashMap<Long, ArrayList<HashMap<String, String>>>> favourites;


    public GuildModel(Long guildId, String prefix, ArrayList<HashMap<Long, ArrayList<HashMap<String, String>>>> favourites) {
        this.guildId = guildId;
        this.prefix = prefix;
        this.favourites = favourites;
    }

    public String getPrefix(){
        return this.prefix;
    }

    @Nullable
    public ArrayList<HashMap<String, String>> getFavouritesOfUser(long userId) {
        for(HashMap<Long, ArrayList<HashMap<String, String>>> map : favourites) {
            if(map.containsKey(userId)) {
                return map.get(userId);
            }
        }
        return null;
    }

    public void setPrefix(String newPrefix) {
        this.prefix = newPrefix;
    }

    public void addFavourites(HashMap<String, String> newTrack, Long userId) {
        for(HashMap<Long, ArrayList<HashMap<String, String>>> map : favourites) {
            if(map.containsKey(userId)) {
                map.get(userId).add(newTrack);
                return;
            }
        }
    }

    public void addNewUserFavourites(ArrayList<HashMap<String, String>> favs, long userId) {
        HashMap<Long, ArrayList<HashMap<String, String>>> map = new HashMap<>();
        map.put(userId, favs);
        this.favourites.add(map);
    }

    public void clearFavouritesOfUser(Long userId) {
        for(HashMap<Long, ArrayList<HashMap<String, String>>> map : favourites) {
            if(map.containsKey(userId)) {
                map.put(userId, new ArrayList<>());
                return;
            }
        }
    }

    public void removeFavourite(Long userId, String trackId) {
        for(HashMap<Long, ArrayList<HashMap<String, String>>> map : favourites) {
            if(map.containsKey(userId)) {
                ArrayList<HashMap<String, String>> hashMaps = map.get(userId);
                for(HashMap<String, String> entry : hashMaps) {
                    if(entry.get("_id") != null && entry.get("_id").equals(trackId)) {
                        List<HashMap<String, String>> upd = hashMaps.stream().filter((item) -> !item.get("_id").equals(trackId)).collect(Collectors.toList());
                        map.put(userId, (ArrayList<HashMap<String, String>>) upd);
                        return;
                    }
                }
                return;
            }
        }
    }

}
