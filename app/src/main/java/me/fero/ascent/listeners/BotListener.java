package me.fero.ascent.listeners;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotListener extends BaseListener {

    private ScheduledExecutorService service;
    public static final Logger LOGGER = LoggerFactory.getLogger(BotListener.class);
    private boolean turn = true;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());

        this.jda = event.getJDA();

        this.service = Executors.newScheduledThreadPool(1, (r) -> new Thread(r, "Status-Update-Thread"));

        service.scheduleAtFixedRate(() -> {
            if(turn) {
                List<Guild> guilds = this.jda.getGuilds();
                long amount = 0;
                for(Guild guild : guilds) {
                    amount += guild.getMemberCount();
                }
                this.jda.getPresence().setActivity(Activity.playing("music for " + amount + " members"));
            }
            else {
                this.jda.getPresence().setActivity(Activity.listening("help in " + event.getGuildTotalCount() + " Guilds"));
            }

            turn = !turn;
        }, 0, 2, TimeUnit.HOURS);
    }
}
