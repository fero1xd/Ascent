package me.fero.ascent.utils;
import me.fero.ascent.commands.setup.ICommand;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CooldownUtil {

    // userId <--> Time when cooldown end
    private static final HashMap<Long, Timestamp> cooldowns = new HashMap<>();
    private static ScheduledExecutorService  service = Executors.newSingleThreadScheduledExecutor((r) -> {
        final Thread thread = new Thread(r, "Command-cooldown-Thread");
        thread.setDaemon(true);
        return thread;
    });

    static {
        service.scheduleWithFixedDelay(() -> {
            for(Map.Entry<Long, Timestamp> entry : cooldowns.entrySet()) {
                Timestamp tmstp = entry.getValue();

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                if(timestamp.compareTo(tmstp) > 0) {
                    cooldowns.remove(entry.getKey());
                }
            }

        }, 0, 5, TimeUnit.MINUTES);
    }

    public static long checkCooldownForUser(long userId, ICommand cmd) {
        if(cmd.cooldownInSeconds() <= 0) return -1;

        Timestamp currTime = new Timestamp(System.currentTimeMillis());
        int cooldownTime = cmd.cooldownInSeconds() * 1000;

        if(cooldowns.containsKey(userId)) {
            Timestamp expTime = cooldowns.get(userId);

            if(expTime.compareTo(currTime) > 0) {
                long timeDiff = expTime.getTime() - currTime.getTime();
                return TimeUnit.MILLISECONDS.toSeconds(timeDiff);
            }
            else {
                cooldowns.remove(userId);
                cooldowns.put(userId, new Timestamp(currTime.getTime() + cooldownTime));
            }
        }
        else {
            cooldowns.put(userId, new Timestamp(currTime.getTime() + cooldownTime));
        }
        return -1;
    }
}
