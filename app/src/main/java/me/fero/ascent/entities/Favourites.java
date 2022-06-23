package me.fero.ascent.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Favourites implements Serializable {

    private final Long userId;
    private ArrayList<SavableTrack> tracks;

    public Favourites(Long userId, ArrayList<SavableTrack> tracks) {
        this.userId = userId;
        this.tracks = tracks;
    }

    public Long getUserId() {
        return this.userId;
    }

    public ArrayList<SavableTrack> getFavourites() {
        return this.tracks;
    }

    public void addFavourite(SavableTrack trackToSave) {
        this.tracks.add(trackToSave);
    }

    public void clearFavourites() {
        this.tracks.clear();
    }

    public void removeFavourite(String id) {
        this.tracks = (ArrayList<SavableTrack>) this.tracks.stream().filter(track -> track.getId() != null && !track.getId().equals(id)).collect(Collectors.toList());
    }
}
