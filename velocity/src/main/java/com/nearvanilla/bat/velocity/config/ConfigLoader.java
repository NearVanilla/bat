package com.nearvanilla.bat.velocity.config;

import com.google.inject.Inject;
import com.nearvanilla.bat.velocity.BatVelocityPlugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import java.io.File;
import java.nio.file.Path;

/**
 * Loads and provides configuration objects.
 */
public class ConfigLoader {

    private static final @NonNull String CONFIG_HEADER =
        """
        bat - tablist.conf
        ------------------
        This configuration uses MiniMessage formatting for strings.
        In addition to the default MiniMessage tags, the following tags are supported:
        - <proxycount> - the total players connected to the proxy
        - <proxymax> - the max amount of players able to be connected to the proxy
        - <proxymotd> - the proxy's motd
        - <servercount> - the total players connected to the player's current server
        - <servermax> - the max amount of players able to be connected to the server
        - <servermotd> - the server's motd
        - <playerping> - the player's ping
        - <playeruuid> - the player's uuid
        - <playername> - the player's name
        - <playerip> - the player's ip
        """;

    private final @NonNull BatVelocityPlugin plugin;
    private final @NonNull Path dataDirectory;
    private @MonotonicNonNull PluginConfig pluginConfig;

    @Inject
    public ConfigLoader(final @NonNull BatVelocityPlugin plugin,
                        final @NonNull @DataDirectory Path dataDirectory) {
        this.plugin = plugin;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Reloads {@link PluginConfig}
     */
    public void reloadConfig() {
        this.pluginConfig = this.loadConfiguration();
    }

    /**
     * Returns the {@link PluginConfig}.
     *
     * @return the config
     */
    public @NonNull PluginConfig batConfig() {
        if (this.pluginConfig == null) {
            reloadConfig();
        }

        return this.pluginConfig;
    }

    private @NonNull PluginConfig loadConfiguration() {
        final File configFile = new File(dataDirectory.toFile(), "bat.conf");

        final @NonNull HoconConfigurationLoader loader = HoconConfigurationLoader
                .builder()
                .defaultOptions(opts -> opts
                        .shouldCopyDefaults(true)
                        .header(CONFIG_HEADER)
                )
                .file(configFile)
                .build();


        try {
            @NonNull CommentedConfigurationNode node = loader.load();
            final PluginConfig config = ObjectMapper.factory().get(PluginConfig.class).load(node);
            if (config == null) {
                throw new RuntimeException("Config is null");
            }
            loader.save(node);
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
