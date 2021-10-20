package com.nearvanilla.bat.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nearvanilla.bat.velocity.tab.TablistService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
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
        version = "1.0.0",
        description = "Basic, awesome TAB plugin",
        url = "nearvanilla.com"
)
public class BatVelocityPlugin {

    private final @NonNull ProxyServer server;
    private final @NonNull Logger logger;
    private final @NonNull Injector injector;

    private @MonotonicNonNull TablistService tablistService;

    /**
     * Constructs {@code BatVelocityPlugin}.
     *
     * @param logger the logger
     */
    @Inject
    public BatVelocityPlugin(final @NonNull Logger logger,
                             final @NonNull ProxyServer server,
                             final @NonNull Injector injector) {
        this.server = server;
        this.logger = logger;
        this.injector = injector;
    }

    /**
     * Handles {@link ProxyInitializeEvent}.
     *
     * @param event the event
     */
    @Subscribe
    public void onProxyInitialization(final @NonNull ProxyInitializeEvent event) {
        this.enable();
    }

    @Subscribe
    public void onPlayerJoin(final @NonNull ServerPostConnectEvent event) {
        final Player player = event.getPlayer();
        this.tablistService.updateTablist(player);
    }

    public void enable() {
        this.tablistService = this.injector.getInstance(TablistService.class);
        this.tablistService.enable();
    }
}
