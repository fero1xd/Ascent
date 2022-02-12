package me.fero.ascent;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.commands.commands.*;
import me.fero.ascent.commands.commands.music.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager(EventWaiter waiter) {
        addCommand(new Ping());
        addCommand(new Help(this));
        addCommand(new Join());
        addCommand(new Play());
        addCommand(new Stop());
        addCommand(new Skip());
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
        addCommand(new Remove());
        addCommand(new Search(waiter));
        addCommand(new ChangePrefix());
        addCommand(new Invite());
        addCommand(new Profile());
        addCommand(new Move());
        addCommand(new DevInfo());
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));
        if(nameFound) {
            throw new IllegalArgumentException("A Command with this name is already present");
        }
        commands.add(cmd);
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();

        for(ICommand cmd : commands){
            if(cmd.getName().equalsIgnoreCase(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }
        return null;
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


            if(cmd.getType().equalsIgnoreCase("music")) {
                MusicCommand.handleMusicCommands(ctx, cmd);
                return;
            }
            cmd.handle(ctx);
        }
    }


}
