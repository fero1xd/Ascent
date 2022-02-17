package me.fero.ascent;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.VeryBadDesign;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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


    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());

        this.jda = event.getJDA();
        this.jda.getPresence().setActivity(Activity.listening("help in " + event.getGuildTotalCount() + " Guilds"));
    }


    public Listener(EventWaiter waiter) {
        this.manager = new CommandManager(waiter);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {


        TextChannel defaultChannel = event.getGuild().getDefaultChannel();
        if(defaultChannel !=null) {
            defaultChannel.sendMessageEmbeds(Embeds.introEmbed(event.getGuild().getSelfMember(), DatabaseManager.INSTANCE.getPrefix(event.getGuild().getIdLong())).build()).queue();
        }

        this.jda.getPresence().setActivity(Activity.listening("help in " + event.getJDA().getGuilds().size() + " Guilds"));
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        this.jda.getPresence().setActivity(Activity.listening("help on " + event.getJDA().getGuilds().size() + " Guilds"));
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        User user = event.getAuthor();


        if(user.isBot() || event.isWebhookMessage()) {
            return;
        }

        final long guildId = event.getGuild().getIdLong();
        String prefix = VeryBadDesign.PREFIXES.computeIfAbsent(guildId, (id) -> DatabaseManager.INSTANCE.getPrefix(guildId));


        String raw = event.getMessage().getContentRaw();



        User selfUser = event.getGuild().getSelfMember().getUser();
        String mention = "<@!" + selfUser.getId() + ">";

        if(raw.contains(mention)) {
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

            if(copy.isEmpty() && canClose) {
                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                musicManager.scheduler.isRepeating = false;
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.player.stopTrack();

                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
            }

        }
//        else if(event.getMember().getUser() == event.getGuild().getSelfMember().getUser()){
//            if(event.getChannelLeft() != null && event.getChannelJoined() == null) {
//                GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
//
//                musicManager.scheduler.isRepeating = false;
//                musicManager.scheduler.queue.clear();
//                musicManager.scheduler.player.stopTrack();
//            }
//        }
    }


}
