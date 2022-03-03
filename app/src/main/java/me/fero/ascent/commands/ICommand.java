package me.fero.ascent.commands;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);

    String getName();
    String getHelp();
    default String getUsage() {
        return null;
    };

    default List<String> getAliases() {
        return List.of();
    }
    default String getType() {
        return "";
    }
    default boolean isDjNeeded() {
        return false;
    }
    default int cooldownInSeconds() {
        return 0;
    }

}
