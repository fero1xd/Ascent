package me.fero.ascent.commands.commands.general;

import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.database.MongoDbDataSource;
import me.fero.ascent.database.RedisDataStore;
import me.fero.ascent.objects.BaseCommand;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashSet;
import java.util.List;

public class Ignore extends BaseCommand {

    public Ignore() {
        this.name = "ignore";
        this.help = "Ignores command for current channel";
        this.aliases = List.of("ig");
        this.userPermissions = List.of(Permission.MANAGE_SERVER);
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

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
}
