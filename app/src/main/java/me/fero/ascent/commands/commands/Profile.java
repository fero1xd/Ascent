package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Profile implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();


        channel.sendMessageEmbeds(Embeds.showProfileEmbed(member).build()).queue();

    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public String getHelp() {
        return "Shows the profile of the user who executed this command";
    }
}
