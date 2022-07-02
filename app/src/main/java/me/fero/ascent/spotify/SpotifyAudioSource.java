package me.fero.ascent.spotify;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.audio.AudioTrackInfoWithImage;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.listeners.BaseListener;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// Taken from https://github.com/DuncteBot/SkyBot/blob/main/src/main/java/ml/duncte123/skybot/audio/sourcemanagers/spotify/SpotifyAudioSourceManager.java (Added)
public class SpotifyAudioSource implements AudioSourceManager, SpotifyAudioSourceManager {
    private SpotifyApi spi = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseListener.class);
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
    public final YoutubeAudioSourceManager youtubeAudioSourceManager;

    public SpotifyAudioSource(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
        this.spi =  new SpotifyApi.Builder()
                .setClientId(AscentConfig.get("spotify_client_id"))
                .setClientSecret(AscentConfig.get("spotify_client_secret"))
                .build();
        this.service = Executors.newScheduledThreadPool(2, (r) -> new Thread(r, "Spotify-Token-Update-Thread"));
        service.scheduleAtFixedRate(this::updateAccessToken, 0, 1, TimeUnit.HOURS);
    }


    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference ref) {
        AudioItem item = getTrack(ref);

        if (item == null) {
            item = getPlaylist(ref);
        }

        if (item == null) {
            item = getAlbum(ref);
        }

        return item;
    }

    public AudioItem getTrack(AudioReference reference) {

        final Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);
        if(!res.matches()) {
            return null;
        }
        try {
            GetTrackRequest request = this.spi.getTrack(res.group(res.groupCount())).build();
            Track track = request.execute();

            return buildTrack(track);
        }
         catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
         }

        return null;
    }

    public AudioItem getPlaylist(AudioReference reference) {
        final Matcher res = SPOTIFY_PLAYLIST_REGEX.matcher(reference.identifier);
        if(!res.matches()) {
            return null;
        }
        try {

            final Playlist spotifyPlaylist = this.spi.getPlaylist(res.group(res.groupCount())).build().execute();

            List<PlaylistTrack> playlistTracks = List.of(spotifyPlaylist.getTracks().getItems());
            if(playlistTracks.isEmpty()) {
                return null;
            }

            final int originalSize = playlistTracks.size();
            if(originalSize > TrackScheduler.MAX_QUEUE_SIZE) {
                playlistTracks = playlistTracks.subList(0, TrackScheduler.MAX_QUEUE_SIZE);
            }

            final List<AudioTrack> finalPlaylist = new ArrayList<>();

            for(final PlaylistTrack playlistTrack  : playlistTracks) {
                if (playlistTrack.getIsLocal()) {
                    continue;
                }

                final IPlaylistItem item = playlistTrack.getTrack();
                if(item instanceof Track) {
                    Track track = (Track) item;
                    finalPlaylist.add(buildTrack(track));
                }
            }

            if (finalPlaylist.isEmpty()) {
                throw new FriendlyException("This playlist does not contain playable tracks (podcasts cannot be played)", FriendlyException.Severity.COMMON, null);
            }

            return new SpotifyPlaylist(spotifyPlaylist.getName(), finalPlaylist, null, false, originalSize);
        }
        catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AudioItem getAlbum(AudioReference reference) {
        final Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);
        if(!res.matches()) {
            return null;
        }

        try {
            final Future<Album> albumFuture = this.spi.getAlbum(res.group(res.groupCount())).build().executeAsync();

            final Album album = albumFuture.get();
            TrackSimplified[] items = album.getTracks().getItems();
            Image[] images = album.getImages();
            final List<AudioTrack> playList = new ArrayList<>();

            for(final TrackSimplified track : items) {
                playList.add(buildTrackFromSimple(track, images));
            }

            return new BasicAudioPlaylist(album.getName(), playList, null, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void searchTrack(CommandContext ctx, String query, EventWaiter waiter) {
        try {
            TextChannel channel = ctx.getChannel();
            Paging<Track> execute = this.spi.searchTracks(query).build().execute();

            Track[] items = execute.getItems();

            if(items.length == 0) {
                ctx.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "Couldn't find anything on spotify", null, null, null).build()).queue();
                return;
            }

            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            builder.setDescription("Select a track. You have 15 seconds");
            final int trackCount = Math.min(items.length, 20);

            String s = UUID.randomUUID().toString();
            SelectionMenu.Builder menu = SelectionMenu.create(s);
            menu.setPlaceholder("Select your track here");
            menu.setRequiredRange(1, 1);

            for (int i = 0; i <  trackCount; i++) {
                final Track track = items[i];
                menu.addOption(track.getName(), track.getId(), track.getArtists()[0].getName());
            }

            channel.sendMessageEmbeds(builder.build()).setActionRow(menu.build()).queue((message) -> {
                        waiter.waitForEvent(
                                SelectionMenuEvent.class,
                                (e) -> {
                                    if(!(e.getChannel() == channel && e.getMember() != null && !e.getMember().getUser().isBot() && e.getComponentId().equals(s))) {
                                        return false;
                                    }
                                    if(e.getMember() != ctx.getMember()) {
                                        e.reply("This menu is not for you").setEphemeral(true).queue();
                                        return false;
                                    }
                                    return true;
                                },
                                (e) -> {
                                    String uri = "https://open.spotify.com/track/" + e.getValues().get(0) ;
                                    LavalinkPlayerManager.getInstance().loadAndPlay(ctx, uri, message, true);
                            },
                    15, TimeUnit.SECONDS,
                    () -> {
                        message.delete().queue();
                    }
                );
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAccessToken() {

        try {
            final ClientCredentialsRequest request = this.spi.clientCredentials().build();
            final ClientCredentials clientCredentials = request.execute();

            this.spi.setAccessToken(clientCredentials.getAccessToken());

            LOGGER.info("Successfully retrieved access token!");
            LOGGER.info("The access token expires in " + clientCredentials.getExpiresIn() + " seconds");

        }catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }


    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new SpotifyAudioTrack(trackInfo, AscentConfig.get("yt_key"), this);
    }

    @Override
    public void shutdown() {
        if(this.service != null) {
            this.service.shutdown();
        }
    }

    private AudioTrack buildTrackFromSimple(TrackSimplified track, Image[] images) {
        return new SpotifyAudioTrack(
                new AudioTrackInfoWithImage(
                        track.getName(),
                        track.getArtists()[0].getName(),
                        track.getDurationMs(),
                        track.getId(),
                        false,
                        track.getExternalUrls().get("spotify"),
                        getImageOrDefault(images)
                ),
                AscentConfig.get("yt_key"),
                this
        );
    }

    private AudioTrack buildTrack(Track track) {
        return new SpotifyAudioTrack(
                new AudioTrackInfoWithImage(
                        track.getName(),
                        track.getArtists()[0].getName(),
                        track.getDurationMs(),
                        track.getId(),
                        false,
                        track.getExternalUrls().get("spotify"),
                        getImageOrDefault(track.getAlbum().getImages())
                ),
                AscentConfig.get("yt_key"),
                this
        );
    }

    private String getImageOrDefault(Image[] images) {
        if (images.length > 0) {
            return images[0].getUrl();
        }

        return "https://dunctebot.com/img/favicon.png";
    }
}
