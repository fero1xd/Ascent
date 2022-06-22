package me.fero.ascent;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.commands.commands.*;
import me.fero.ascent.commands.commands.music.*;
import me.fero.ascent.commands.commands.music.Queue;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.CooldownUtil;
import me.fero.ascent.utils.Embeds;
import me.fero.ascent.utils.Waiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandManager {
    private final HashMap<String, ICommand> commands = new HashMap<>();
    private final HashMap<String, String> aliases = new HashMap<>();

    public CommandManager() {
        addCommand(new Ping());
        addCommand(new Help(this));
        addCommand(new Join());
        addCommand(new Play());
        addCommand(new Stop());
        addCommand(new Skip(Waiter.getInstance().waiter));
        addCommand(new NowPlaying());
        addCommand(new Pause());
        addCommand(new Resume());
        addCommand(new Queue());
        addCommand(new Clear());
        addCommand(new Volume());
        addCommand(new Loop());
        addCommand(new Leave());
        addCommand(new Seek());
        addCommand(new Restart());
        addCommand(new ScPlay());
        addCommand(new Shuffle());
        addCommand(new Remove(Waiter.getInstance().waiter));
        addCommand(new Search(Waiter.getInstance().waiter));
        addCommand(new ChangePrefix());
        addCommand(new Invite());
        addCommand(new Profile());
        addCommand(new Move());
        addCommand(new DevInfo());
        addCommand(new ForceSkip());
        addCommand(new Spotify(Waiter.getInstance().waiter));
        addCommand(new Favourite());
        addCommand(new RmDuplicates());
        addCommand(new GetFav());
        addCommand(new ClearFav());
        addCommand(new LoadFav());
        addCommand(new RemoveFav(Waiter.getInstance().waiter));
        addCommand(new Quote());
        addCommand(new Banner());
        addCommand(new Ignore());
        addCommand(new Vote());
        addCommand(new BassBoost());
        addCommand(new DevLogs());
    }

    private void addCommand(ICommand cmd) {
        String name = cmd.getName();

        if (name.contains(" ")) {
            throw new IllegalArgumentException(" Name can't have spaces!");
        }

        final String cmdName = name.toLowerCase();

        if (this.commands.containsKey(cmdName)) {
            throw new IllegalArgumentException(String.format("Command %s already present", cmdName));
        }

        final List<String> lowerAliasses = cmd.getAliases().stream().map(String::toLowerCase).collect(Collectors.toList());

        if (!lowerAliasses.isEmpty()) {
            for (final String alias : lowerAliasses) {
                if (this.aliases.containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                            "Alias %s already present (Stored for: %s, trying to insert: %s))",
                            alias,
                            this.aliases.get(alias),
                            name
                    ));
                }

                if (this.commands.containsKey(alias)) {
                    throw new IllegalArgumentException(String.format(
                            "Alias %s already present for command (Stored for: %s, trying to insert: %s))",
                            alias,
                            this.commands.get(alias).getClass().getSimpleName(),
                            cmd.getClass().getSimpleName()
                    ));
                }
            }

            for (final String alias : lowerAliasses) {
                this.aliases.put(alias, name);
            }
        }

        this.commands.put(cmdName, cmd);
    }

    public HashMap<String, ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();

        ICommand cmd = this.commands.get(searchLower);
        if(cmd == null) {
            final String forAlias = this.aliases.get(searchLower);

            if (forAlias != null) {
                cmd = this.commands.get(forAlias);
            }
        }

        return cmd;
    }

   void handle(GuildMessageReceivedEvent event, String prefix) {
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if(cmd != null) {
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            event.getChannel().sendTyping().queue();


            // Check for ignored Channels
            HashSet<String> ignoredChannels = RedisDataStore.getInstance().getIgnoredChannels(ctx.getGuild().getIdLong());
            if(ignoredChannels.contains(event.getChannel().getId()) && !cmd.getName().equalsIgnoreCase("ignore")) return;

            // Check for cool down
            long l = CooldownUtil.checkCooldownForUser(event.getMember().getIdLong(), cmd);
            if(l > 0) {
                event.getChannel().sendMessageEmbeds(Embeds.createBuilder("Cool down error!", "Please wait " + l + " seconds before using this command", null, null, null).build()).queue();
                return;
            }

            if(cmd.getType().equalsIgnoreCase("music")) {
                MusicCommand.handleMusicCommands(ctx, cmd);
                return;
            }
            cmd.handle(ctx);
        }
    }
}
