package com.nearvanilla.bat.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nearvanilla.bat.velocity.command.Commands;
import com.nearvanilla.bat.velocity.config.ConfigLoader;
import com.nearvanilla.bat.velocity.listener.LuckPermsListener;
import com.nearvanilla.bat.velocity.tab.TablistService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

/**
 * bat's Velocity entrypoint.
 */
@Plugin(
        id = "bat",
        name = "bat",
        version = "1.1.4",
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

    private @MonotonicNonNull MinecraftChannelIdentifier vanishChannel;

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
        // Resolve config via injector to avoid constructor-injecting ConfigLoader,
        // which would create a Guice circular dependency on the plugin class.
        final ConfigLoader configLoader = this.injector.getInstance(ConfigLoader.class);
        final String channelName = configLoader.batConfig().vanishChannel;
        this.vanishChannel = MinecraftChannelIdentifier.from(channelName);
        server.getChannelRegistrar().register(this.vanishChannel);
    }

    @Subscribe
    public void onPlayerSwitch(final @NonNull ServerPostConnectEvent event) {
        this.tablistService.handleServerConnection(event.getPlayer());
    }

    @Subscribe
    public void onPlayerQuit(final @NonNull DisconnectEvent event) {
        this.tablistService.handlePlayerLeave(event.getPlayer());
    }

    @Subscribe
    public void onProxyPing(final @NonNull ProxyPingEvent event) {
        if (this.tablistService == null) {
            return;
        }

        if (event.getPing().getPlayers().isEmpty()) {
            return;
        }

        final int visibleCount = this.tablistService.getVisiblePlayerCount();
        final ServerPing ping = event.getPing().asBuilder()
                .onlinePlayers(visibleCount)
                .build();
        event.setPing(ping);
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        // Check if the identifier matches first, no matter the source.
        // this allows setting all messages to IDENTIFIER as handled,
        // preventing any client-originating messages from being forwarded.
        if (this.vanishChannel == null || !this.vanishChannel.equals(event.getIdentifier())) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        // only attempt parsing the data if the source is a backend server
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        final UUID playerUuid = UUID.fromString(in.readUTF());
        final boolean vanished = in.readBoolean();

        this.tablistService.setPlayerVanishedState(playerUuid, vanished);
    }

    public void enable() {
        this.tablistService = this.injector.getInstance(TablistService.class);
        this.tablistService.enable();

        this.commands = this.injector.getInstance(Commands.class);
        this.commands.register();

        if (this.server.getPluginManager().isLoaded("LuckPerms")) {
            this.server.getEventManager().register(this, this.injector.getInstance(LuckPermsListener.class));
        }
    }
}
