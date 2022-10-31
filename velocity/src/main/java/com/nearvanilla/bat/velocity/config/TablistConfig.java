package com.nearvanilla.bat.velocity.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

/**
 * Holds configuration data for a tab list.
 */
@ConfigSerializable
public class TablistConfig {

    @Comment("""
            A list of strings (with MiniMessage formatting) to use as the tablist's header.
            """)
    public @NonNull List<String> headerFormatStrings = List.of(
            "",
            "<bold><gradient:#69d1c8:#61b1d4>bat</gradient></bold>",
            "",
            "<gray><italic>The basic, awesome tablist plugin.</gray>",
            "",
            "<color:#f2d68f>Current time:</color> <color:#91e9ed><datetime></color>",
            ""
    );

    @Comment("""
            A list of strings (with MiniMessage formatting) to use as the tablist's footer.
            """)
    public @NonNull List<String> footerFormatStrings = List.of(
            "",
            "<italic><bold><gradient:#69d1c8:#61b1d4>bat</gradient></bold> <gray>supports a variety of placeholders.",
            "<gray><italic>This is an incomplete list of all supported placeholders. Read the",
            "<gray><italic>documentation for a full breakdown of all placeholders.",
            "",
            "<gray>\\<<aqua>proxycount</aqua>> - </gray><gray><proxycount></gray>",
            "<gray>\\<<aqua>proxymax</aqua>> - </gray><gray><proxymax></gray>",
            "<gray>\\<<aqua>proxymotd</aqua>> - </gray><gray><proxymotd></gray>",
            "<gray>\\<<aqua>servercount</aqua>> - </gray><gray><servercount></gray>",
            "<gray>\\<<aqua>servermax</aqua>> - </gray><gray><servermax></gray>",
            "<gray>\\<<aqua>servermotd</aqua>> - </gray><gray><servermotd></gray>",
            "<gray>\\<<aqua>playerping</aqua>> - </gray><gray><playerping>ms</gray>",
            "<gray>\\<<aqua>playeruuid</aqua>> - </gray><gray><playeruuid></gray>",
            "<gray>\\<<aqua>playername</aqua>> - </gray><gray><playername></gray>",
            "<gray>\\<<aqua>playerip</aqua>> - </gray><gray><playerip></gray>",
            ""
    );

}
