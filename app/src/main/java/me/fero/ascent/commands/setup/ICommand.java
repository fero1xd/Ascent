package me.fero.ascent.commands.setup;

import java.util.List;

public interface ICommand {
    void handle(CommandContext ctx);

    String getName();
    String getHelp();
    default String getUsage(String prefix) {
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
    default boolean mayAutoJoin() { return false; }
}
