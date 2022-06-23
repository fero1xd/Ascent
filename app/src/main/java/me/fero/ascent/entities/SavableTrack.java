package me.fero.ascent.entities;

import java.io.Serializable;

public class SavableTrack implements Serializable {
    private String _id;
    private String name;
    private String artist;
    private String link;
    private String identifier;
    private String user;

    public SavableTrack(String _id, String name, String artist, String link, String identifier, String user) {
        this._id = _id;
        this.name = name;
        this.artist = artist;
        this.link = link;
        this.identifier = identifier;
        this.user = user;
    }

    public SavableTrack() {
    }

    // GETTERS

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getLink() {
        return link;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getUser() {
        return user;
    }

    // SETTERS

    public void setId(String _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
