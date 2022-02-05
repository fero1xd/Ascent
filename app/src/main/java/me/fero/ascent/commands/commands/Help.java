package me.fero.ascent.commands.commands;

import me.fero.ascent.CommandManager;
import me.fero.ascent.Config;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.VeryBadDesign;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.List;

public class Help implements ICommand {
    private final CommandManager manager;

    public Help(CommandManager manager){
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        String prefix = VeryBadDesign.PREFIXES.get(ctx.getGuild().getIdLong());
        if(args.isEmpty()) {
            StringBuilder musicBuilder = new StringBuilder();
            StringBuilder generalBuilder = new StringBuilder();


            for(ICommand cmd : manager.getCommands()) {
                if(cmd.getType().equalsIgnoreCase("music")) {
                    musicBuilder.append("`").append(prefix).append(cmd.getName()).append("` ").append(cmd.getHelp()).append("\n");
                }
                else {
                    generalBuilder.append("`").append(prefix).append(cmd.getName()).append("` ").append(cmd.getHelp()).append("\n");

                }
            }

            EmbedBuilder builder1 = Embeds.helpEmbed(ctx.getMember());

            builder1.setDescription("Use "  + prefix + "help <cmd_name> for more help");
            builder1.addField("General 🧬", generalBuilder.toString(), false);
            builder1.addField("Music 📯", musicBuilder.toString(), false);



            channel.sendMessageEmbeds(builder1.build()).queue();

            return;
        }

        String search = args.get(0);
        ICommand command = manager.getCommand(search);
        if(command == null) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Nothing found", null, null, Color.RED).build()).queue();
            return;
        }

        if(command.getUsage() != null) {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, prefix + command.getUsage(), null, null, null).build()).queue();
        }
        else {
            channel.sendMessageEmbeds(Embeds.createBuilder(null, command.getHelp(), null, null, null).build()).queue();
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Shows the list of commands";
    }

    @Override
    public List<String> getAliases() {
        return List.of("commands", "cmds", "commandlist");
    }
}
