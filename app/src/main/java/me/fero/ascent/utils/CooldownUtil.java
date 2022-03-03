package me.fero.ascent.utils;
import me.fero.ascent.commands.ICommand;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CooldownUtil {

//    private static final HashMap<String, HashMap<Long, Timestamp>> cooldowns = new HashMap<>();

    private static final HashMap<Long, Timestamp> cooldowns = new HashMap<>();

    public static long checkCooldownForUser(long userId, ICommand cmd) {
        if(cmd.cooldownInSeconds() <= 0) return -1;

        Timestamp currTime = new Timestamp(System.currentTimeMillis());

        int cooldownTime = cmd.cooldownInSeconds() * 1000;

        if(cooldowns.containsKey(userId)) {
            Timestamp expTime = new Timestamp(cooldowns.get(userId).getTime() + cooldownTime);
            if(expTime.compareTo(currTime) > 0) {
                long timeDiff = expTime.getTime() - currTime.getTime();
                return TimeUnit.MILLISECONDS.toSeconds(timeDiff);
            }
        }

        cooldowns.put(userId, currTime);
        setTimeout(() -> cooldowns.remove(userId), cooldownTime);
        return -1;
    }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
