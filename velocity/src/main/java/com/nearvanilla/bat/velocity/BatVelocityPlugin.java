package com.nearvanilla.bat.velocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nearvanilla.bat.velocity.command.Commands;
import com.nearvanilla.bat.velocity.listener.LuckPermsListener;
import com.nearvanilla.bat.velocity.tab.TablistService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
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
        version = "1.0.1",
        authors = {"Bluely_", "Prof_Bloodstone"},
        description = "Basic, awesome TAB plugin",
        url = "nearvanilla.com"
)
public class BatVelocityPlugin {

    private final @NonNull ProxyServer server;
    private final @NonNull Logger logger;
    private final @NonNull Injector injector;

    private @MonotonicNonNull TablistService tablistService;
    private @MonotonicNonNull Commands commands;

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

    @Subscribe
    public void onProxyInitialization(final @NonNull ProxyInitializeEvent event) {
        this.enable();
    }

    @Subscribe
    public void onPlayerSwitch(final @NonNull ServerPostConnectEvent event) {
        this.tablistService.handleServerConnection(event.getPlayer());
    }

    @Subscribe
    public void onPlayerQuit(final @NonNull DisconnectEvent event) {
        this.tablistService.handlePlayerLeave(event.getPlayer());
    }

    public void enable() {
        this.tablistService = this.injector.getInstance(TablistService.class);
        this.tablistService.enable();

        this.commands = this.injector.getInstance(Commands.class);
        this.commands.register();

        if (this.server.getPluginManager().isLoaded("luckperms")) {
            this.injector.getInstance(LuckPermsListener.class);
        }
    }
}
