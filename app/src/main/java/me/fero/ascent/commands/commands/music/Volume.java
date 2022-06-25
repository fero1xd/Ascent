package me.fero.ascent.commands.commands.music;

import lavalink.client.player.LavalinkPlayer;
import me.fero.ascent.audio.GuildMusicManager;
import me.fero.ascent.commands.setup.CommandContext;
import me.fero.ascent.commands.setup.ICommand;
import me.fero.ascent.lavalink.LavalinkPlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Volume implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        GuildMusicManager musicManager = LavalinkPlayerManager.getInstance().getMusicManager(ctx.getGuild());
        LavalinkPlayer audioPlayer = musicManager.player;

        if(args.isEmpty()) {
            int result = audioPlayer.getVolume();
            EmbedBuilder builder = Embeds.createBuilder(null, "Current volume is " + result + "%", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        int amount = -1;
        try {
            amount = Integer.parseInt(args.get(0));
        } catch(NumberFormatException e) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Please pass a value between 0-100 for volume", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        if(amount < 0 || amount > 100) {
            EmbedBuilder builder = Embeds.createBuilder("Error!", "Please pass a value between 0-100 for volume", null, null, null);
            channel.sendMessageEmbeds(builder.build()).queue();
            return;
        }

        audioPlayer.getFilters().setVolume(amount / 100F).commit();


        EmbedBuilder builder = Embeds.createBuilder(null, "Volume changed", "NOTE - This may take a while", null, null);
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getHelp() {
        return "Sets the volume of the player";
    }

    @Override
    public String getType() {
        return "music";
    }

    @Override
    public String getUsage(String prefix) {
        return prefix + "volume <0-100>";
    }

    @Override
    public List<String> getAliases() {
        return List.of("vol");
    }
}
