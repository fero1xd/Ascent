package me.fero.ascent.commands.commands.music.filters;

import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;


public class BassBoost extends BaseFilter {

    public BassBoost() {
        this.name = "bassboost";
        this.help = "Enables BassBoost (experimental)";
        this.usage = "bassboost <0-100>";
        this.maxAmount = 200;
    }

    @Override
    public void execute(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();

        int amount = Integer.parseInt(ctx.getArgs().get(0));
        Guild guild = ctx.getGuild();


        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(guild);
        musicManager.getScheduler().bassBoost(amount);

        channel.sendMessageEmbeds(Embeds.createBuilder("Success!", "Changed bass boost value",
                "NOTE - This may take a while", null, null).build()).queue();
    }
}
