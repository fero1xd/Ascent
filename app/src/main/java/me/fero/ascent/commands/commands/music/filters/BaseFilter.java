package me.fero.ascent.commands.commands.music.filters;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public abstract class BaseFilter implements ICommand {

    protected String name;
    protected boolean isDjNeeded = true;
    protected String type = "music";
    protected String usage;
    protected String help;
    protected boolean needInt = true;
    protected int maxAmount = 200;
    protected int cooldown = 10;

    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        String prefix = RedisDataStore.getInstance().getPrefix(ctx.getGuild().getIdLong());

        List<String> args = ctx.getArgs();
        if(args.isEmpty()) {
            sendCorrectUsage(channel, prefix);
            return;
        }

        if(needInt) {
            try {
                int amount = Integer.parseInt(args.get(0));
                if(amount <= 0 || amount > maxAmount) {
                    sendCorrectUsage(channel, prefix);
                    return;
                }
            }catch (NumberFormatException e) {
                sendCorrectUsage(channel, prefix);
                return;
            }
        }

        execute(ctx);
    }

    protected void sendCorrectUsage(TextChannel channel, String prefix) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setDescription("Correct usage is " + prefix + this.getName() + " <0-100>");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public abstract void execute(CommandContext ctx);

    @Override
    public String getName() {
        return this.name;
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
    public String getHelp() {
        return this.help;
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + this.usage;
    }

    @Override
    public int cooldownInSeconds() {
        return cooldown;
    }
}
