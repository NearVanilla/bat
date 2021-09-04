package com.nearvanilla.bat.velocity;

import com.google.inject.Inject;
import com.nearvanilla.bat.velocity.config.BatConfig;
import com.nearvanilla.bat.velocity.config.ConfigLoader;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

/**
 * bat's Velocity entrypoint.
 */
@Plugin(
        id = "bat",
        name = "bat",
        version = "@version@",
        description = "Basic, awesome TAB plugin",
        url = "nearvanilla.com"
)
public class BatVelocityPlugin {

    private final @NonNull ProxyServer server;
    private final @NonNull Logger logger;
    private final @NonNull ConfigLoader configLoader;

    private @MonotonicNonNull BatConfig config;

    /**
     * Constructs {@code BatVelocityPlugin}.
     *
     * @param logger the logger
     */
    @Inject
    public BatVelocityPlugin(final @NonNull Logger logger,
                             final @NonNull ProxyServer server,
                             final @NonNull ConfigLoader configLoader) {
        this.server = server;
        this.logger = logger;
        this.configLoader = configLoader;
    }

    /**
     * Handles {@link ProxyInitializeEvent}.
     *
     * @param event the event
     */
    @Subscribe
    public void onProxyInitialization(final @NonNull ProxyInitializeEvent event) {
        this.config = this.configLoader.batConfig();
    }
}
