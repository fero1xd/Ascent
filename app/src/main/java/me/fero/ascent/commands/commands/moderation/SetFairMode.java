package me.fero.ascent.commands.commands.moderation;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.DatabaseManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class SetFairMode implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();

        List<String> args = ctx.getArgs();
        if(args.isEmpty()) {
            boolean isUsingFairMode = DatabaseManager.INSTANCE.isUsingFairMode(ctx.getGuild().getIdLong());
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Fair mode is " + (isUsingFairMode ? "On" : "Off"), null, null, null).build()).queue();
            return;
        }

        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Missing Permissions", null, null, null).build()).queue();

            return;
        }

        if(args.get(0).equalsIgnoreCase("true") || args.get(0).equalsIgnoreCase("on")) {
            DatabaseManager.INSTANCE.setFairMode(ctx.getGuild().getIdLong(), true);
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Fair mode set to On", null, null, null).build()).queue();
        }
        else if(args.get(0).equalsIgnoreCase("false") || args.get(0).equalsIgnoreCase("off")) {
            DatabaseManager.INSTANCE.setFairMode(ctx.getGuild().getIdLong(), false);
            channel.sendMessageEmbeds(Embeds.createBuilder(null, "Fair mode set to Off", null, null, null).build()).queue();

        }
        else {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Wrong arguments", null, null, null).build()).queue();
        }
    }

    @Override
    public String getName() {
        return "fairmode";
    }

    @Override
    public String getHelp() {
        return "Sets the fair mode for the server";
    }

    @Override
    public String getUsage() {
        return "fairmode on/off";
    }
}
