package com.nearvanilla.bat.velocity.tab;

import com.nearvanilla.bat.velocity.BatVelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ServerDataProvider {

    private final @NonNull ProxyServer server;
    private final @NonNull BatVelocityPlugin plugin;
    private final @NonNull Map<String, ServerPing> pingMap;
    private final @NonNull Map<UUID, Integer> playerPingMap;
    private final @NonNull ScheduledTask updateTask;

    @Inject
    public ServerDataProvider(final @NonNull ProxyServer server,
                              final @NonNull BatVelocityPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
        this.pingMap = new ConcurrentHashMap<>();
        this.playerPingMap = new ConcurrentHashMap<>();
        this.updateTask = server.getScheduler()
                .buildTask(this.plugin, this::update)
                .repeat(5L, TimeUnit.SECONDS)
                .schedule();
    }

    /**
     * Returns {@code serverName}'s max players.
     *
     * @param serverName the server name
     * @return the max players
     */
    public int getMaxPlayers(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            final Optional<ServerPing.Players> players = this.pingMap.get(serverName).getPlayers();

            if (players.isPresent()) {
                return players.get().getMax();
            }
        }

        return 0;
    }

    /**
     * Returns the MOTD from the server.
     *
     * @param serverName the server name
     * @return the name
     */
    public @NonNull Component getMotd(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            return this.pingMap.get(serverName).getDescriptionComponent();
        }

        return Component.text("");
    }


    /**
     * Returns {@code serverName}'s online players.
     *
     * @param serverName the server name
     * @return the online players
     */
    public int getPlayers(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            final Optional<ServerPing.Players> players = this.pingMap.get(serverName).getPlayers();

            if (players.isPresent()) {
                return players.get().getOnline();
            }
        }

        return 0;
    }

    /**
     * Pings all servers and stores the ping information in a map.
     */
    private void update() {
        final var servers = this.server.getAllServers();

        for (final RegisteredServer server : servers) {
            server.ping().thenAcceptAsync(ping -> this.pingMap.put(server.getServerInfo().getName(), ping));
        }

        for (final Player player : this.server.getAllPlayers()) {
            this.playerPingMap.put(player.getUniqueId(), (int) player.getPing());
        }
    }

}
