package me.fero.ascent.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Embeds {

    public static EmbedBuilder createBuilder(String title, String description, String footer, String footerUrl, Color color) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();

        if(title != null) {
            builder.setTitle(title);
        }
        if(description != null) {
            builder.setDescription(description);
        }
        if(footer != null) {
            if(footerUrl != null) {
                builder.setFooter(footer, footerUrl);
            }
            else {
                builder.setFooter(footer);
            }
        }
        if(color != null) {
            builder.setColor(color);
        }
        else {
            Color color1 = new Color((int) (Math.random() * 0x1000000));
            builder.setColor(color1);
        }


        return builder;
    }

    public static EmbedBuilder notConnectedToVcEmbed(Member member) {
        // Embeds
        String title = "Error!";
        String description = "You need to be in a voice channel";
        String footer = "Requested by " + member.getEffectiveName();
        String footerUrl = member.getEffectiveAvatarUrl();
        return Embeds.createBuilder(title, description, footer, footerUrl, null);

    }

    public static EmbedBuilder alreadyConnectedToVcEmbed(Member member) {
        // Embeds
        String title = "Error!";
        String description = "Already connected to a voice channel";
        String footer = "Requested by " + member.getEffectiveName();
        String footerUrl = member.getEffectiveAvatarUrl();
        return Embeds.createBuilder(title, description, footer, footerUrl, null);

    }

    public static EmbedBuilder notEnoughPermsEmbed(Member member) {
        // Embeds
        String title = "Error!";
        String description = "I don't have correct permissions to join the voice channel";
        String footer = "Requested by " + member.getEffectiveName();
        String footerUrl = member.getEffectiveAvatarUrl();
        return Embeds.createBuilder(title, description, footer, footerUrl, null);

    }

    public static EmbedBuilder notInSameVcEmbed(Member member) {
        // Embeds
        String title = "Error!";
        String description = "You need to be in the same voice channel as me for this to work";
        String footer = "Requested by " + member.getEffectiveName();
        String footerUrl = member.getEffectiveAvatarUrl();
        return Embeds.createBuilder(title, description, footer, footerUrl, null);

    }

    public static EmbedBuilder queueIsEmptyEmbed() {
        // Embeds
        String title = "Error!";
        String description = "The queue is currently empty";
        return Embeds.createBuilder(title, description, null, null, null);

    }

    public static EmbedBuilder songEmbed(Member member, AudioTrack track) {
        // Embeds
        String title = "Added to queue 💿";
        String description = "[" + track.getInfo().title + " - " + track.getInfo().author + "]" + "(" + track.getInfo().uri + ")";

        EmbedBuilder builder = Embeds.createBuilder(title, description, member != null ? "Requested by " + member.getEffectiveName() : null, member != null ? member.getEffectiveAvatarUrl()  : null, null);
        boolean isStream = track.getInfo().isStream;

        long millis = track.getDuration();
        String formattedCurrent;
        if(millis >= 3600000) {
            formattedCurrent = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

        }
        else {
            formattedCurrent = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }



        builder.addField("Duration", track.getInfo().isStream ? "This is a live stream" : formattedCurrent + " minutes", false);

        if(isStream) {
            builder.addField("Live","Yes", false);
        }


        builder.setThumbnail("https://i1.ytimg.com/vi/" + track.getIdentifier() + "/hqdefault.jpg");
        return builder;

    }


    public static void sendSongEmbed(Member member, AudioTrack track, TextChannel channel) {
        EmbedBuilder builder = songEmbed(member, track);

        Button btn = Button.danger("addToFavourite", Emoji.fromMarkdown("🤍"));
        Button pauseBtn = Button.primary("pause", Emoji.fromMarkdown("<:pause:988040572596027422>"));
        Button skipBtn = Button.primary("skip", Emoji.fromMarkdown("<:skip:988041074398355467>"));

        channel.sendMessageEmbeds(builder.build()).setActionRow(btn, pauseBtn, skipBtn).queue();
    }

    public static EmbedBuilder songEmbedWithoutDetails(AudioTrack track) {
        return createBuilder(null, "Added " + "**[" + track.getInfo().title + "](" + track.getInfo().uri + ")** by **" + track.getInfo().author + "** to the queue",
                null, null, null);
    }

    public static List<Button> getControls(boolean pause) {
        Button btn = Button.danger("addToFavourite", Emoji.fromMarkdown("🤍"));


        Button pauseBtn = Button.primary(pause ? "pause" : "resume", Emoji.fromMarkdown(pause ? "<:pause:988040572596027422>" : "<:resume:988040811960745995>"));
        Button skipBtn = Button.primary("skip", Emoji.fromMarkdown("<:skip:988041074398355467>"));

        return List.of(btn, pauseBtn, skipBtn);
    }

    public static EmbedBuilder showProfileEmbed(Member member) {
        // Embeds
        EmbedBuilder builder = Embeds.createBuilder(null, null, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), Color.YELLOW);
        builder.setThumbnail(member.getEffectiveAvatarUrl());
        builder.addField("Profile", member.getEffectiveName() + "#" + member.getUser().getDiscriminator(), false);
        builder.addField("ID", member.getId(), false);

        List<Role> roles = member.getRoles();

        List<String> roleNames = new ArrayList<>();
        for(Role role : roles) {
            roleNames.add(role.getName());
        }


        builder.addField("Roles", roleNames.isEmpty() ? "No Roles" :String.join(", ", roleNames), false);

        return builder;

    }
    public static EmbedBuilder helpEmbed(Member member) {
        // Embeds

        EmbedBuilder builder = Embeds.createBuilder(null, null, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), Color.YELLOW);
        builder.setTitle("Help");
        builder.setThumbnail(member.getGuild().getIconUrl());


        return builder;

    }

    public static EmbedBuilder introEmbed(Member jda, String prefix) {
        String title = "Ascent";
        String body  = "Thanks for adding ascent to your server..\nType " + prefix + "help for more info";

        EmbedBuilder builder = Embeds.createBuilder(title, body, "Created by " + Config.get("DEV_NAME"), null, Color.RED).setThumbnail(jda.getEffectiveAvatarUrl());
        return builder;
    }
}
