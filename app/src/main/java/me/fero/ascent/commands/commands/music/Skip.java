package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.audio.TrackScheduler;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;

import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Skip implements ICommand {

    private EventWaiter waiter = null;

    public Skip(EventWaiter waiter) {
        this.waiter = waiter;
    }
    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        VoiceChannel vc = ctx.getSelfMember().getVoiceState().getChannel();


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        LavalinkPlayer audioPlayer = musicManager.player;
        TrackScheduler scheduler = musicManager.getScheduler();

        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        if(scheduler.votingGoingOn) {
            EmbedBuilder builder = Embeds.createBuilder(null, "A voting is going on already. Please be patient !", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        List<Member> filteredMembers = new ArrayList<>();

        for(Member member : vc.getMembers()) {
            if(!member.getUser().isBot()) {
                filteredMembers.add(member);
            }
        }

        if(filteredMembers.size() >= 3) {
            scheduler.initializeVotingSystem(filteredMembers);

            String unicode = "U+1F44D";
            channel.sendMessageEmbeds(Embeds.createBuilder("Voting starts now",
                    "React with :thumbsup: to vote for next song . I will wait for 20 seconds",
                    "Requested by " +
                            ctx.getMember().getEffectiveName(), ctx.getMember().getEffectiveAvatarUrl(),
                    null).build()).queue((message) -> {
                        message.addReaction(unicode).queue();
                        this.waiter.waitForEvent(
                                GuildMessageReactionAddEvent.class,
                                (e) -> {
                                    if(e.getMember().getUser().isBot() ||
                                            !e.getReactionEmote().isEmoji() ||
                                            !e.getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDC4D") ||
                                            !e.getMessageId().equals(message.getId())) return false;

                                    if (!e.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                                        message.removeReaction(unicode).queue();
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().inVoiceChannel()) {
                                        message.removeReaction(unicode).queue();
                                        e.getReaction().removeReaction().queue();
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().getChannel().getId().equalsIgnoreCase(e.getGuild().getSelfMember().getVoiceState().getChannel().getId())) {
                                        e.getReaction().removeReaction().queue();

                                        return false;
                                    }
                                    if (scheduler.votingGoingOn) {
                                        if (scheduler.votes.contains(e.getMember())) {
                                            e.getReaction().removeReaction().queue();
                                            return false;
                                        }

                                        if(!scheduler.totalMembers.contains(e.getMember())) {
                                            scheduler.totalMembers.add(e.getMember());
                                        }
                                        scheduler.votes.add(e.getMember());

                                        return scheduler.votes.size() >= Math.ceil(scheduler.totalMembers.size() / 2f);
                                    }

                                    return false;
                                },
                                (e) -> {
                                    if(scheduler.votes.size() >= Math.ceil(scheduler.totalMembers.size() / 2f)) {

                                        scheduler.resetVotingSystem();

                                        // Skip the song
                                        scheduler.nextTrack();
                                        message.delete().queue();

                                        ctx.getMessage().addReaction("👍").queue();
                                    }

                                },
                                20, TimeUnit.SECONDS,
                                () -> {
                                    if(scheduler.votingGoingOn) {
                                        scheduler.resetVotingSystem();
                                        message.delete().queue();
                                        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Voting did not come to an end . Try again next time..", null, null, null).build()).queue();
                                    }

                                }
                        );

                        this.waiter.waitForEvent(
                                GuildMessageReactionRemoveEvent.class,
                                (e) -> {

                                    if(e.retrieveMember().complete().getUser().isBot() || !e.getReactionEmote().isEmoji() || !e.getReactionEmote().getEmoji().equalsIgnoreCase("\uD83D\uDC4D") || !e.getMessageId().equals(message.getId())) return false;

                                    if (!e.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().inVoiceChannel()) {
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().getChannel().getId().equalsIgnoreCase(e.getGuild().getSelfMember().getVoiceState().getChannel().getId())) {
                                        return false;
                                    }
                                    if (scheduler.votingGoingOn) {
                                        if (scheduler.votes.contains(e.getMember())) {
                                            scheduler.votes = scheduler.votes.stream().filter((vote) -> !vote.getId().equals(e.getMember().getId())).collect(Collectors.toList());

                                            return false;
                                        }

                                        return scheduler.votes.size() >= Math.ceil(scheduler.totalMembers.size() / 2f);
                                    }

                                    return false;
                                },
                                (e) -> {
                                    if(scheduler.votes.size() >= Math.ceil(scheduler.totalMembers.size() / 2f)) {

                                        scheduler.resetVotingSystem();

                                        // Skip the song
                                        scheduler.nextTrack();
                                        message.delete().queue();
                                        ctx.getMessage().addReaction("👍").queue();
                                    }

                                },
                                20, TimeUnit.SECONDS,
                                () -> {
                                }
                        );
                    });

            return;
        }

        scheduler.nextTrack();
        ctx.getMessage().addReaction("👍").queue();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp() {
        return "Skips the current song";
    }


    @Override
    public List<String> getAliases() {
        return List.of("s");
    }

    @Override
    public String getType() {
        return "music";
    }
    @Override
    public int cooldownInSeconds() {
        return 5;
    }
}
