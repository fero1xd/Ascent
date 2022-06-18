package me.fero.ascent.commands.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.fero.ascent.commands.CommandContext;
import me.fero.ascent.commands.ICommand;
import me.fero.ascent.lavaplayer.GuildMusicManager;
import me.fero.ascent.lavaplayer.PlayerManager;
import me.fero.ascent.utils.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Volume implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        List<String> args = ctx.getArgs();

        GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(ctx.getGuild());
        AudioPlayer audioPlayer = musicManager.scheduler.player;

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

        audioPlayer.setVolume(amount);
        EmbedBuilder builder = Embeds.createBuilder(null, "Volume changed", null, null, null);
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
