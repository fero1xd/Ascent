package me.fero.ascent.commands.commands.music;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class BassBoost implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        List<String> args = ctx.getArgs();
        Guild guild = ctx.getGuild();
        String prefix = RedisDataStore.getInstance().getPrefix(guild.getIdLong());

        if(args.isEmpty()) {
            sendCorrectUsage(channel, prefix);
            return;
        }

        int amount = -1;

        try {
            amount = Integer.parseInt(args.get(0));
            if(amount < 0 || amount > 200) {
                sendCorrectUsage(channel, prefix);
                return;
            }
        }catch (NumberFormatException e) {
            sendCorrectUsage(channel, prefix);
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        musicManager.scheduler.bassBoost(amount);

        channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Changed bass boost value",
                "NOTE - This feature is currently experimental", null, null).build()).queue();
    }

    private void sendCorrectUsage(TextChannel channel, String prefix) {
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed();
        builder.setDescription("Correct usage is " + prefix + this.getName() + " <0-100>");
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "bassboost";
    }

    @Override
    public String getHelp() {
        return "Enables BassBoost (experimental)";
    }

    @Override
    public boolean isDjNeeded() {
        return true;
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "bassboost <0-100>";
    }
}
