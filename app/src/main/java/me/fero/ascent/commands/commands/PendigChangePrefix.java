package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class PendigChangePrefix implements ICommand {


    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        if(args.isEmpty()) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Incorrect usage", null, null, null).build()).queue();
            return;
        }

        String newPrefix = ctx.getArgs().get(0);
        if(newPrefix.length() > 5) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Too long prefix", null, null, null).build()).queue();
            return;
        }

        if(newPrefix.length() == 0) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Too short prefix", null, null, null).build()).queue();
            return;
        }

        try {

//            Connection conn = Database.connect();
//            Statement stmt = conn.createStatement();

//            stmt.executeQuery("UPDATE guilds SET prefix='" + newPrefix + "' WHERE guildId="+ctx.getGuild().getIdLong() + ";");


            channel.sendMessage("Success").queue();

        } catch (Exception e) {
            e.printStackTrace();
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Some error occurred when changing prefix", null, null, null).build()).queue();
        }

    }

    @Override
    public String getName() {
        return "cp";
    }

    @Override
    public String getHelp() {
        return "Changes the prefix of the bot to the given one for the server";
    }
}
