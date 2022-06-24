package me.fero.ascent.commands.commands.general;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Profile extends BaseCommand {

    public Profile() {
        this.name = "profile";
        this.help = "Shows the profile of the user who executed this command";
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();


        channel.sendMessageEmbeds(Embeds.showProfileEmbed(member).build()).queue();

    }
}
