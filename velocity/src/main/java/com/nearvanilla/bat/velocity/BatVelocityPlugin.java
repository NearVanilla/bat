package com.nearvanilla.bat.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
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

    private final @NonNull Logger logger;

    /**
     * Constructs {@code BatVelocityPlugin}.
     *
     * @param logger the logger
     */
    @Inject
    public BatVelocityPlugin(final @NonNull Logger logger) {
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

    }
}
