package me.fero.ascent.objects;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements ICommand {

    protected String name = null;
    protected String help = null;
    protected String usage = null;
    protected List<String> aliases = new ArrayList<>();
    protected String type = "";
    protected boolean isDjNeeded = false;
    protected int cooldownInSeconds = 0;
    protected List<Permission> userPermissions = new ArrayList<>();
    protected List<Permission> botPermissions = new ArrayList<>();
    protected boolean requiredArgs = false;
    protected int requiredArgCount = 1;

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());

        if (this.userPermissions.size() > 0 && !ctx.getMember().hasPermission(channel, this.userPermissions)) {
            final String permissionsWord = "permission" + (this.userPermissions.size() > 1 ? "s" : "");
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Not enough " + permissionsWord, null, null, null).build()).queue();
            return;
        }

        if (this.botPermissions.size() > 0 && !ctx.getSelfMember().hasPermission(channel, this.botPermissions)) {
            final String permissionsWord = "permission" + (this.botPermissions.size() > 1 ? "s" : "");

            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "I do not have enough " + permissionsWord, null, null, null).build()).queue();
            return;
        }

        if (this.requiredArgs &&
                (ctx.getArgs().isEmpty() || ctx.getArgs().size() < this.requiredArgCount)
        ) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Correct usage is  " + getUsage(prefix), null, null, null).build()).queue();
            return;
        }

        execute(ctx);
    }

    public abstract void execute(CommandContext ctx);

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getHelp() {
        return this.help;
    }

    @Override
    public String getUsage(String prefix) {
        return usage != null ? "`" + prefix + this.name + " " + this.usage + "`" : null;
    }

    @Override
    public List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isDjNeeded() {
        return isDjNeeded;
    }

    @Override
    public int cooldownInSeconds() {
        return cooldownInSeconds;
    }
}
