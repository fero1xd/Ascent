package me.fero.ascent;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.commands.commands.*;
import me.fero.ascent.commands.commands.music.*;
import me.fero.ascent.utils.CooldownUtil;
import me.fero.ascent.utils.Embeds;
import me.fero.ascent.utils.Waiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

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
//        addCommand(new Lyrics());

        addCommand(new Vote());

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
