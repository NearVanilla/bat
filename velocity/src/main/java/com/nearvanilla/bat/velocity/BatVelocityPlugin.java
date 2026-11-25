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
        version = "1.1.5",
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

    private @MonotonicNonNull MinecraftChannelIdentifier spectatorChannel;
    private @MonotonicNonNull MinecraftChannelIdentifier statusChannel;

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
        final String spectatorChannelName = configLoader.batConfig().spectatorChannel;
        this.spectatorChannel = MinecraftChannelIdentifier.from(spectatorChannelName);
        server.getChannelRegistrar().register(this.spectatorChannel);

        final String statusChannelName = configLoader.batConfig().statusChannel;
        this.statusChannel = MinecraftChannelIdentifier.from(statusChannelName);
        server.getChannelRegistrar().register(this.statusChannel);
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
        final boolean isSpectatorChannel = this.spectatorChannel != null && this.spectatorChannel.equals(event.getIdentifier());
        final boolean isStatusChannel = this.statusChannel != null && this.statusChannel.equals(event.getIdentifier());
        if (!isSpectatorChannel && !isStatusChannel) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        // only attempt parsing the data if the source is a backend server
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        final ServerConnection connection = (ServerConnection) event.getSource();
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            final UUID playerUuid = UUID.fromString(in.readUTF());

            if (isStatusChannel) {
                this.handleStatusMessage(connection, playerUuid, in);
            } else {
                final boolean vanished = in.readBoolean();

                this.tablistService.setPlayerVanishedState(playerUuid, vanished);
            }
        } catch (final IllegalArgumentException | IllegalStateException e) {
            this.logger.warn("Ignoring a malformed {} message from {}",
                    event.getIdentifier().getId(), connection.getServerInfo().getName());
        }
    }

    /**
     * Sets a player's status from a status message, unless it came from a server the player is not on.
     *
     * @param connection the server connection the message came from
     * @param playerUuid the UUID of the player the message is about
     * @param in         the rest of the message: the status (UTF), empty for none
     */
    private void handleStatusMessage(final @NonNull ServerConnection connection, final @NonNull UUID playerUuid,
                                     final @NonNull ByteArrayDataInput in) {
        final String serverName = connection.getServerInfo().getName();
        // Only the server the player is on sets their status; a late message from the
        // server they just left is ignored.
        final boolean fromCurrentServer = connection.getPlayer().getUniqueId().equals(playerUuid)
                && connection.getPlayer().getCurrentServer()
                .map(current -> current.getServerInfo().getName().equals(serverName))
                .orElse(false);
        if (!fromCurrentServer) {
            return;
        }
        this.tablistService.setPlayerStatus(playerUuid, serverName, in.readUTF());
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
