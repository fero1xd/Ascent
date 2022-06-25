package me.fero.ascent.commands.commands.music;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;

import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import me.fero.ascent.youtube.YoutubeAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Search implements ICommand {
    final private EventWaiter waiter;

    public Search(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        if(ctx.getArgs().isEmpty()) {
            channel.sendMessage("Correct usage is " + AscentConfig.get("prefix") + "play <youtube_link>/").queue();
            return;
        }

        String query = String.join(" ", ctx.getArgs());


        query = query.replace("<", "");
        query = query.replace(">", "");

        if (isUrl(query)) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "No links allowed", null, null, null).build()).queue();
            return;
        }

        try {
            List<SearchResult> res = YoutubeAPI.searchYoutube(query, AscentConfig.get("yt_key"), 10);

            if(res.isEmpty()) {
                EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
                builder.setTitle("Error!");
                builder.setDescription("❌ No tracks found");
                channel.sendMessageEmbeds(builder.build()).queue();
            }


            EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
            builder.setDescription("Select a track. You have 15 seconds");

            final int trackCount = Math.min(res.size(), 20);

            String s = UUID.randomUUID().toString();
            SelectionMenu.Builder menu = SelectionMenu.create(s);
            menu.setPlaceholder("Select your track here");
            menu.setRequiredRange(1, 1);

            for (int i = 0; i <  trackCount; i++) {
                final SearchResult track = res.get(i);
                SearchResultSnippet snippet = track.getSnippet();

                menu.addOption(snippet.getTitle(), track.getId().getVideoId(), snippet.getChannelTitle());
            }

            channel.sendMessageEmbeds(builder.build()).setActionRow(menu.build()).queue((message -> waiter.waitForEvent(
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
                        String id = e.getValues().get(0);

                        if(id != null) {
                            String url = "https://youtube.com/watch?v=" + id;
                            LavalinkPlayerManager.getInstance().loadAndPlay(ctx, url, message, true);
                        }
                    },
                    15, TimeUnit.SECONDS,
                    () -> { message.delete().queue(); }
            )));

        } catch (Exception ignored) {}
    }

    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getHelp() {
        return "Searches song";
    }

    private boolean isUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "search <track_name>";
    }

    @Override
    public int cooldownInSeconds() {
        return 5;
    }

    @Override
    public boolean mayAutoJoin() {
        return true;
    }
}
