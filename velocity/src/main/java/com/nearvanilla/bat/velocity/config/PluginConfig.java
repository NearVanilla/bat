package com.nearvanilla.bat.velocity.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

/**
 * Contains the plugin's configuration.
 */
public class PluginConfig {

    @Comment("""
            Controls how many milliseconds should pass between tablist updates.
            """)
    public long updateFrequency = 1000;

    @Comment("""
            Stores tablist configuration.
            """)
    public @NonNull Map<String, TablistConfig> tablists = Map.of(
            "default", new TablistConfig()
    );

    @Comment("""
            The default tablist to show players.
            """)
    public @NonNull String defaultTablist = "default";

    @Comment("""
            The format of a player's name in the tablist. Supports MiniMessage and every bat placeholder.
            """)
    public @NonNull String playerNameFormat = "<groupcode><playername><servercode>";

    @Comment("""
            If a player is in a LuckPerms group defined in this map, then the template "<groupcode>" will return it's
            value in this map. Otherwise, "<groupcode>" will return nothing ("").
            """)
    public @NonNull Map<String, String> groupCodes = Map.of(
            "admin", "<red><bold>A</bold></red> "
    );

    @Comment("""
            If a player is present on a server defined in this map, then the template "<servercode>" will return it's
            value in this map. Otherwise, "<servercode>" will return nothing ("").
            """)
    public @NonNull Map<String, String> serverCodes = Map.of(
            "survival", " <dark_gray>[<gray>S</gray>]</dark_gray>"
    );

}
