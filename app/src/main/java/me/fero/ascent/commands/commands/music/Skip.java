package me.fero.ascent.commands.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;

import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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


        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.audioPlayer;

        if(audioPlayer.getPlayingTrack() == null) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "No track playing", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        if(musicManager.scheduler.votingGoingOn) {
            EmbedBuilder builder = Embeds.createBuilder(null, "A voting is going on already. Please be patient !", null,null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }


        List<Member> filteredMembers = vc.getMembers().stream().filter(member -> !member.getUser().isBot()).collect(Collectors.toList());
        if(filteredMembers.size() >= 3) {
            musicManager.scheduler.votingGoingOn = true;
            musicManager.scheduler.votes.clear();
            musicManager.scheduler.totalMembers.clear();
            musicManager.scheduler.totalMembers = filteredMembers;

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
                                    if(e.getMember().getUser().isBot() || !e.getReactionEmote().isEmoji() || !e.getReactionEmote().getEmoji().equalsIgnoreCase(unicode) || !e.getMessageId().equals(message.getId())) return false;

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
                                    if (musicManager.scheduler.votingGoingOn) {
                                        if (musicManager.scheduler.votes.contains(e.getMember())) {
                                            e.getReaction().removeReaction().queue();

                                            return false;
                                        }

                                        if(!musicManager.scheduler.totalMembers.contains(e.getMember())) {
                                            musicManager.scheduler.totalMembers.add(e.getMember());
                                        }
                                        musicManager.scheduler.votes.add(e.getMember());

                                        return musicManager.scheduler.votes.size() >= Math.ceil(musicManager.scheduler.totalMembers.size() / 2f);
                                    }

                                    return false;
                                },
                                (e) -> {
                                    if(musicManager.scheduler.votes.size() >= Math.ceil(musicManager.scheduler.totalMembers.size() / 2f)) {

                                        musicManager.scheduler.totalMembers.clear();
                                        musicManager.scheduler.votes.clear();
                                        musicManager.scheduler.votingGoingOn = false;

                                        // Skip the song
                                        musicManager.scheduler.nextTrack();
                                        message.delete().queue();

                                        EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
                                        channel.sendMessageEmbeds(builder.build()).queue();
                                    }

                                },
                                20, TimeUnit.SECONDS,
                                () -> {
                                    if(musicManager.scheduler.votingGoingOn) {
                                        musicManager.scheduler.totalMembers.clear();
                                        musicManager.scheduler.votes.clear();
                                        musicManager.scheduler.votingGoingOn = false;

                                        message.delete().queue();
                                        channel.sendMessageEmbeds(Embeds.createBuilder(null, "Voting did not come to an end . Try again next time..", null, null, null).build()).queue();
                                    }

                                }
                        );

                        this.waiter.waitForEvent(
                                GuildMessageReactionRemoveEvent.class,
                                (e) -> {

                                    if(e.retrieveMember().complete().getUser().isBot() || !e.getReactionEmote().isEmoji() || !e.getReactionEmote().getEmoji().equalsIgnoreCase(unicode) || !e.getMessageId().equals(message.getId())) return false;

                                    if (!e.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().inVoiceChannel()) {
                                        return false;
                                    }

                                    if (!e.getMember().getVoiceState().getChannel().getId().equalsIgnoreCase(e.getGuild().getSelfMember().getVoiceState().getChannel().getId())) {
                                        return false;
                                    }
                                    if (musicManager.scheduler.votingGoingOn) {
                                        if (musicManager.scheduler.votes.contains(e.getMember())) {
                                            musicManager.scheduler.votes = musicManager.scheduler.votes.stream().filter((vote) -> !vote.getId().equals(e.getMember().getId())).collect(Collectors.toList());

                                            return false;
                                        }

                                        return musicManager.scheduler.votes.size() >= Math.ceil(musicManager.scheduler.totalMembers.size() / 2f);
                                    }

                                    return false;
                                },
                                (e) -> {
                                    if(musicManager.scheduler.votes.size() >= Math.ceil(musicManager.scheduler.totalMembers.size() / 2f)) {

                                        musicManager.scheduler.totalMembers.clear();
                                        musicManager.scheduler.votes.clear();
                                        musicManager.scheduler.votingGoingOn = false;

                                        // Skip the song
                                        musicManager.scheduler.nextTrack();
                                        message.delete().queue();

                                        EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
                                        channel.sendMessageEmbeds(builder.build()).queue();
                                    }

                                },
                                20, TimeUnit.SECONDS,
                                () -> {
                                }
                        );
                    });

            return;
        }

        musicManager.scheduler.nextTrack();
        EmbedBuilder builder = Embeds.createBuilder(null, "Skipped the current track", null, null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
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
