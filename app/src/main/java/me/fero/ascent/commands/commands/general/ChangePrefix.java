package me.fero.ascent.commands.commands.general;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ChangePrefix extends BaseCommand {

    public ChangePrefix() {
        this.name = "changeprefix";
        this.aliases = List.of("cp");
        this.help = "Changes the prefix of the bot to the given one for the server";
        this.userPermissions = List.of(Permission.MANAGE_SERVER);
        this.requiredArgs = true;
        this.usage = "<prefix>";
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        final String newPrefix = String.join("", args);

        updatePrefix(ctx.getGuild().getIdLong(), newPrefix);
        channel.sendMessageEmbeds(Embeds.createBuilder("Success", "Prefix has been changed to `" + newPrefix + "`", null, null, null).build()).queue();
    }

    private void updatePrefix(long guildId, String newPrefix) {
        RedisDataStore.getInstance().setPrefix(guildId, newPrefix);
        DatabaseManager.INSTANCE.setPrefix(guildId, newPrefix);

    }
}
