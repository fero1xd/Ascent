package me.fero.ascent.commands.commands.general;

import me.fero.ascent.commands.setup.CommandManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.objects.config.AscentConfig;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.List;

public class Help extends BaseCommand {
    private final CommandManager manager;

    public Help(CommandManager manager){
        this.manager = manager;
        this.name = "help";
        this.help = "Shows the list of commands";
        this.aliases = List.of("commands", "cmds", "commandlist");
    }

    @Override
    public void execute(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());

        Member member = ctx.getMember();
        if(args.isEmpty()) {
            StringBuilder musicBuilder = new StringBuilder();
            StringBuilder generalBuilder = new StringBuilder();


            for(ICommand cmd : manager.getCommands().values()) {
                if(cmd.getType().equalsIgnoreCase("music")) {
                    musicBuilder.append("`").append(cmd.getName()).append("` ");
                }
                else {
                    generalBuilder.append("`").append(cmd.getName()).append("` ");

                }
            }

            EmbedBuilder builder1 = Embeds.helpEmbed(member);

            builder1.setDescription("Use "  + prefix + "help <cmd_name> for more help");
            builder1.addField("General 🧬", generalBuilder.toString(), false);
            builder1.addField("Music 📯", musicBuilder.toString(), false);
            channel.sendMessageEmbeds(builder1.build()).setActionRow(
                    Button.link("https://discord.gg/Z42RjgxQ", "Join the support server"),
                    Button.link(AscentConfig.get("vote_url"), "Vote for me"),
                    Button.link(AscentConfig.get("invite_url"), "Invite me")
            ).queue();
            return;
        }

        String search = args.get(0);
        ICommand command = manager.getCommand(search);

        if(command == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Nothing found", null, null, Color.RED).build()).queue();
            return;
        }


        String name = command.getName();


        EmbedBuilder builder = Embeds.createBuilder(name.substring(0, 1).toUpperCase() + name.substring(1), null, "Requested by " + member.getEffectiveName(), member.getEffectiveAvatarUrl(), null);

        if(command.getHelp() != null) {
            builder.addField("Info", command.getHelp(), false);
        }

        if(command.getUsage(prefix) != null) {
            builder.addField("Usage", command.getUsage(prefix), false);
        }

        channel.sendMessageEmbeds(builder.build()).queue();
    }

}
