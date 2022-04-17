package me.fero.ascent.commands.commands;

import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.database.MongoDbDataSource;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashSet;
import java.util.List;

public class Ignore implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();


        Member member = ctx.getMember();
        if(!member.hasPermission(Permission.MANAGE_SERVER)) {
            channel.sendMessageEmbeds(Embeds.createBuilder("Error!", "Not enough permission", null, null, null).build()).queue();
            return;
        }

        long idLong = ctx.getGuild().getIdLong();
        HashSet<String> ignoredChannels = RedisDataStore.getInstance().getIgnoredChannels(idLong);

        String channelId = channel.getId();
        if(ignoredChannels.contains(channelId)){
            RedisDataStore.getInstance().unIgnoreChannel(idLong, channelId);
            MongoDbDataSource.INSTANCE.unIgnoreChannel(idLong, channelId);
            channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Channel is open Again !", null, null, null).build()).queue();
            return;
        }

        RedisDataStore.getInstance().ignoreChannel(idLong, channelId);
        MongoDbDataSource.INSTANCE.ignoreChannel(idLong, channelId);
        channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Commands will no longer work on this channel", null, null, null).build()).queue();
    }

    @Override
    public String getName() {
        return "ignore";
    }

    @Override
    public String getHelp() {
        return "Ignores command for current channel";
    }

    @Override
    public List<String> getAliases() {
        return List.of("ig");
    }
}
