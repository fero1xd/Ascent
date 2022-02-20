package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ChangePrefix implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();
        Member member = ctx.getMember();

        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "You do not have manage server permission", null, null, null).build()).queue();
            return;
        }
        if(args.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Incorrect usage", null, null, null).build()).queue();
            return;
        }



        final String newPrefix = String.join("", args);

        updatePrefix(ctx.getGuild().getIdLong(), newPrefix);
        channel.sendMessageEmbeds(Embeds.createBuilder("Success", "Prefix has been changed to `" + newPrefix + "`", null, null, null).build()).queue();

    }

    @Override
    public String getName() {
        return "cp";
    }

    @Override
    public String getHelp() {
        return "Changes the prefix of the bot to the given one for the server";
    }

    private void updatePrefix(long guildId, String newPrefix) {
        RedisDataStore.getInstance().setPrefix(guildId, newPrefix);
        DatabaseManager.INSTANCE.setPrefix(guildId, newPrefix);

    }
}
