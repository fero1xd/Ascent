package me.fero.ascent;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.duncte123.botcommons.BotCommons;
import me.fero.ascent.database.SqliteDataSource;
import me.fero.ascent.database.VeryBadDesign;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.xml.crypto.Data;
import java.sql.*;
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
        this.jda.getPresence().setActivity(Activity.listening("help on " + event.getGuildTotalCount() + " Guilds"));
    }


    public Listener(EventWaiter waiter) {
        this.manager = new CommandManager(waiter);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {


        TextChannel defaultChannel = event.getGuild().getDefaultChannel();
        if(defaultChannel !=null) {
            defaultChannel.sendMessageEmbeds(Embeds.introEmbed(event.getGuild().getSelfMember(), this.getPrefix(event.getGuild().getIdLong())).build()).queue();
        }

        this.jda.getPresence().setActivity(Activity.listening("help on " + event.getJDA().getGuilds().size() + " Guilds"));
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
        String prefix = VeryBadDesign.PREFIXES.computeIfAbsent(guildId, (id) -> this.getPrefix(guildId));


        List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
        Member selfMember = event.getGuild().getSelfMember();

        for(Member member : mentionedMembers) {

            if(member == selfMember) {
                event.getChannel().sendMessageEmbeds(Embeds.createBuilder(null, "My prefix is `" + prefix + "`", null, null, null).build()).queue();
                return;
            }
        }


        String raw = event.getMessage().getContentRaw();

        if(raw.equalsIgnoreCase(prefix + "shutdown") &&
                event.getAuthor().getId().equals(Config.get("OWNER_ID"))) {
            LOGGER.info("Shutting Down");
            event.getJDA().shutdown();
            BotCommons.shutdown(event.getJDA());

            return;
        }


        if(raw.startsWith(prefix)) {
            manager.handle(event, prefix);
        }
    }

    private String getPrefix(Long guildId) {
        try(final PreparedStatement stmt = SqliteDataSource.getConnection()
                // language=SQLite
                .prepareStatement("SELECT prefix FROM guild_settings WHERE guild_id=?")) {

            stmt.setString(1, String.valueOf(guildId));

            try(ResultSet resultSet = stmt.executeQuery()) {
                if(resultSet.next()) {
                    return resultSet.getString("prefix");
                }


            }

            try(PreparedStatement insertStatement = SqliteDataSource.getConnection().prepareStatement("INSERT OR IGNORE INTO guild_settings(guild_id) VALUES(?)")) {
                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Config.get("prefix");
    }

}
