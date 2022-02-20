package me.fero.ascent.spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.fero.ascent.Config;
import me.fero.ascent.Listener;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyAudioSource implements SpotifyAudioSourceManager {
    private SpotifyApi spi = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final ScheduledExecutorService service;

    //REGEX
    private static final String PROTOCOL_REGEX = "?:spotify:(track:)|(?:http://|https://)[a-z]+\\.";
    private static final String DOMAIN_REGEX = "spotify\\.com/";
    private static final String TRACK_REGEX = "track/([a-zA-z0-9]+)";
    private static final String ALBUM_REGEX = "album/([a-zA-z0-9]+)";
    private static final String USER_PART = "user/.*/";
    private static final String PLAYLIST_REGEX = "playlist/([a-zA-z0-9]+)";
    private static final String REST_REGEX = ".*";
    private static final String SPOTIFY_BASE_REGEX = PROTOCOL_REGEX + DOMAIN_REGEX;

    private static final Pattern SPOTIFY_TRACK_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + TRACK_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_ALBUM_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ALBUM_REGEX + ")" + REST_REGEX + "$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ")" + PLAYLIST_REGEX + REST_REGEX + "$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX_USER = Pattern.compile("^(" + SPOTIFY_BASE_REGEX + ")" +
            USER_PART + PLAYLIST_REGEX + REST_REGEX + "$");
    private static final Pattern SPOTIFY_SECOND_PLAYLIST_REGEX = Pattern.compile("^spotify(?::user:.*)?:playlist:(.*)$");

    public SpotifyAudioSource() {
        this.spi =  new SpotifyApi.Builder()
                .setClientId(Config.get("spotify_client_id"))
                .setClientSecret(Config.get("spotify_client_secret"))
                .build();
        this.service = Executors.newScheduledThreadPool(2, (r) -> new Thread(r, "Spotify-Token-Update-Thread"));
        service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
    }


    @Override
    public boolean loadItem(CommandContext ctx, String url) {

        if(SPOTIFY_TRACK_REGEX.matcher(url).matches()) {
            this.getTrack(ctx, url);
            return true;
        }
        else if(SPOTIFY_PLAYLIST_REGEX.matcher(url).matches()) {
            this.getPlaylist(ctx, url);
            return true;
        }
        else if(SPOTIFY_ALBUM_REGEX.matcher(url).matches()) {
            this.getAlbum(ctx, url);
            return true;
        }
        return false;
    }

    public void getTrack(CommandContext ctx, String url) {
        final Matcher res = SPOTIFY_TRACK_REGEX.matcher(url);
        if(!res.matches()) {
            return;
        }
        try {
            GetTrackRequest request = this.spi.getTrack(res.group(res.groupCount())).build();
            Track track = request.execute();

            LOGGER.info("Name: "+ track.getName());
            LOGGER.info("Artist: " + track.getArtists()[0].getName());

            String query = "ytsearch:" + track.getName() + " " + track.getArtists()[0].getName();

            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());

            PlayerManager.getInstance().audioPlayerManager.loadItemOrdered(musicManager, query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    System.out.println("track loaded");

                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    boolean empty = playlist.getTracks().isEmpty();
                    if(empty) {
                        return;
                    }

                    AudioTrack audioTrack = playlist.getTracks().get(0);
                    audioTrack.setUserData(ctx.getAuthor().getIdLong());

                    ctx.getChannel().sendMessageEmbeds(Embeds.songEmbed(ctx.getMember(), audioTrack).build()).queue();
                    musicManager.scheduler.queue(audioTrack);
                }

                @Override
                public void noMatches() {
                }

                @Override
                public void loadFailed(FriendlyException exception) {

                }
            });

        }
         catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
         }
    }

    public void getPlaylist(CommandContext ctx, String url) {
        final Matcher res = SPOTIFY_PLAYLIST_REGEX.matcher(url);
        if(!res.matches()) {
            return;
        }
        try {
            final Playlist spotifyPlaylist = this.spi.getPlaylist(res.group(res.groupCount())).build().execute();

            List<PlaylistTrack> playlistTracks = List.of(spotifyPlaylist.getTracks().getItems());
            if(playlistTracks.isEmpty()) {
                return;
            }

            final int originalSize = playlistTracks.size();

            if(originalSize > 100){
                playlistTracks = playlistTracks.subList(0, 100);
            }

            final List<Track> finalPlaylist = new ArrayList<>();

            for(final PlaylistTrack playlistTrack  : playlistTracks) {
                if (playlistTrack.getIsLocal()) {
                    continue;
                }

                final IPlaylistItem item = playlistTrack.getTrack();
                if(item instanceof Track) {
                    Track track = (Track) item;
                    finalPlaylist.add(track);
                }
            }

            ctx.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "Spotify Playlist Loaded : Adding " + finalPlaylist.size() + " Tracks to the queue", null, null, null).build()).queue();
            for(Track track : finalPlaylist) {
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
                final String query = "ytsearch:" + track.getName() + " " + track.getArtists()[0].getName();
                PlayerManager.getInstance().audioPlayerManager.loadItemOrdered(musicManager, query, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {

                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        boolean empty = playlist.getTracks().isEmpty();
                        if(empty) {
                            return;
                        }

                        AudioTrack audioTrack = playlist.getTracks().get(0);
                        audioTrack.setUserData(ctx.getAuthor().getIdLong());

                        musicManager.scheduler.queue(audioTrack);
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
        catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void getAlbum(CommandContext ctx, String url) {
        final Matcher res = SPOTIFY_ALBUM_REGEX.matcher(url);
        if(!res.matches()) {
            return;
        }

        try {
            final Future<Album> albumFuture = this.spi.getAlbum(res.group(res.groupCount())).build().executeAsync();

            final Album album = albumFuture.get();
            TrackSimplified[] items = album.getTracks().getItems();

            ctx.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "Spotify Album Loaded : Adding " + items.length + " Tracks to the queue", null, null, null).build()).queue();

            for(final TrackSimplified track : items) {
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
                final String query = "ytsearch:" + track.getName() + " " + track.getArtists()[0].getName();
                PlayerManager.getInstance().audioPlayerManager.loadItemOrdered(musicManager, query, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {

                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        boolean empty = playlist.getTracks().isEmpty();
                        if(empty) {
                            return;
                        }

                        AudioTrack audioTrack = playlist.getTracks().get(0);
                        audioTrack.setUserData(ctx.getAuthor().getIdLong());

                        musicManager.scheduler.queue(audioTrack);
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAccessToken() {

        try {
            final ClientCredentialsRequest request = this.spi.clientCredentials().build();
            final ClientCredentials clientCredentials = request.execute();

            this.spi.setAccessToken(clientCredentials.getAccessToken());

            LOGGER.info("Successfully retrieved access token! " + clientCredentials.getAccessToken());
            LOGGER.info("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");

        }catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
    }
}