package me.fero.ascent;

import me.fero.ascent.commands.commands.music.*;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Listener extends ListenerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final CommandManager manager;
    private JDA jda;
    private final RedisDataStore redis;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
//        AudioSourceManager.INSTANCE.getTrack("https://open.spotify.com/track/5ABDkxey7t1BJqL8oNt20V?si=oOoIwf4pSPuvhHzG5r1H0Q");


        this.jda = event.getJDA();
        this.jda.getPresence().setActivity(Activity.listening("help in " + event.getGuildTotalCount() + " Guilds"));

//        long guildId = 854330456207654933L;
//        this.jda.getGuildById(guildId).updateCommands().addCommands(
//                        new CommandData("play", "Plays a song from youtube")
//                        .addOption(OptionType.STRING, "query", "Link or search query", true)
//        ).queue();
    }


    public Listener(RedisDataStore redis) {
        this.manager = new CommandManager();
        this.redis = redis;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {


        TextChannel defaultChannel = event.getGuild().getDefaultChannel();
        if(defaultChannel !=null) {
            defaultChannel.sendMessageEmbeds(Embeds.introEmbed(event.getGuild().getSelfMember(), redis.getPrefix(event.getGuild().getIdLong())).build()).queue();
        }
        PlayerManager.getInstance().getMusicManager(event.getGuild());
        LOGGER.info("Joined " + event.getGuild().getName() + " guild Adding the music manager");
        this.jda.getPresence().setActivity(Activity.listening("help in " + event.getJDA().getGuilds().size() + " Guilds"));
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        this.jda.getPresence().setActivity(Activity.listening("help on " + event.getJDA().getGuilds().size() + " Guilds"));

        Member selfMember = event.getGuild().getSelfMember();
        if(selfMember.getVoiceState().inVoiceChannel()) {
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

            musicManager.scheduler.isRepeating = false;
            musicManager.scheduler.queue.clear();
            musicManager.scheduler.player.stopTrack();

            // **RESET EVERYTHING**
            musicManager.scheduler.totalMembers.clear();
            musicManager.scheduler.votes.clear();
            musicManager.scheduler.votingGoingOn = false;

            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.closeAudioConnection();
        }

        PlayerManager.getInstance().removeGuildMusicManager(event.getGuild());

        LOGGER.info("Left " + event.getGuild().getName() + " guild Deleting the music manager");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();


        if(user.isBot() || event.isWebhookMessage()) {
            return;
        }

        final long guildId = event.getGuild().getIdLong();
        String prefix = this.redis.getPrefix(guildId);

        String raw = event.getMessage().getContentRaw();



        User selfUser = event.getGuild().getSelfMember().getUser();
        String mention = "<@" + selfUser.getId() + ">";

        if(raw.trim().equalsIgnoreCase(mention)) {
            event.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "My prefix is `" + prefix + "`", null, null, null).build()).queue();
            return;
        }

        if(raw.startsWith(prefix)) {
            manager.handle(event, prefix);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(event.getMember().getUser() != event.getGuild().getSelfMember().getUser()) {
            VoiceChannel channelLeft = event.getChannelLeft();
            if(channelLeft == null) {
                return;
            }

            List<Member> members = channelLeft.getMembers();
            List<Member> copy = new ArrayList<>();


            boolean canClose = false;
            for(Member member : members) {
                if(member.getUser() != event.getGuild().getSelfMember().getUser()) {
                    copy.add(member);
                }
                else {
                    canClose = true;
                }
            }
            GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

            if(canClose && !copy.isEmpty() && musicManager.scheduler.votingGoingOn){
                musicManager.scheduler.totalMembers = copy;
            }

            if(copy.isEmpty() && canClose) {

                musicManager.scheduler.isRepeating = false;
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.deleteLastSongEmbed();

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;


                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
            }

        }
        else if(event.getMember().getUser() == event.getGuild().getSelfMember().getUser()){
            if(event.getChannelLeft() != null && event.getChannelJoined() != null) {
                VoiceChannel channelJoined = event.getChannelJoined();

                List<Member> members = channelJoined.getMembers();
                List<Member> copy = new ArrayList<>();


                boolean canClose = false;
                for(Member member : members) {
                    if(member.getUser() != event.getGuild().getSelfMember().getUser()) {
                        copy.add(member);
                    }
                    else {
                        canClose = true;
                    }
                }
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;

                if(copy.isEmpty() && canClose) {

                    musicManager.scheduler.isRepeating = false;
                    musicManager.scheduler.queue.clear();
                    musicManager.scheduler.player.stopTrack();
                    musicManager.scheduler.deleteLastSongEmbed();

                    AudioManager audioManager = event.getGuild().getAudioManager();
                    audioManager.closeAudioConnection();

                }
            }
            else if(event.getChannelLeft() != null && event.getChannelJoined() == null){

                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                musicManager.scheduler.isRepeating = false;
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.deleteLastSongEmbed();

                // **RESET EVERYTHING**
                musicManager.scheduler.totalMembers.clear();
                musicManager.scheduler.votes.clear();
                musicManager.scheduler.votingGoingOn = false;

            }
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String componentId = event.getComponentId();

        // Vc check
        Member member = event.getMember();
        int code = MusicCommand.checkVc(member, event.getGuild().getSelfMember());

        if(code != 0) {
//            if(code == -1) {
//                event.replyEmbeds(Embeds.notConnectedToVcEmbed(member).build()).setEphemeral(true).queue();
//            }
//            else if (code == -2) {
//                EmbedBuilder builder = Embeds.notInSameVcEmbed(member);
//                builder.setDescription("Bot must be present in a VoiceChannel to use this Command");
//                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
//            }
//            else if (code == -3) {
//                event.replyEmbeds(Embeds.notInSameVcEmbed(member).setDescription("Already connected to a different channel").build()).setEphemeral(true).queue();
//            }

            // Will not respond
            return;
        }

        switch (componentId) {
            case "addToFavourite":
                Favourite.addToFavourite(true, event, null);
                break;
            case "pause":
                Pause.pause(true, event, null);
                break;
            case "resume":
                Resume.resume(true, event, null);
                break;
            case "skip":
                ForceSkip.forceSkip(true, event, null);
                break;
        }
    }
}
