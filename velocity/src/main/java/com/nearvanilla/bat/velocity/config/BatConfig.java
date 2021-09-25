package com.nearvanilla.bat.velocity.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Contains the plugin's configuration.
 */
public class BatConfig {

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

    public @NonNull String defaultTablist = "default";

}
