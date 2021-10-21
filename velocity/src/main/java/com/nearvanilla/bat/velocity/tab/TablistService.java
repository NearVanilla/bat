package com.nearvanilla.bat.velocity.tab;

import com.google.inject.Inject;
import com.nearvanilla.bat.velocity.BatVelocityPlugin;
import com.nearvanilla.bat.velocity.config.ConfigLoader;
import com.nearvanilla.bat.velocity.config.PluginConfig;
import com.nearvanilla.bat.velocity.config.TablistConfig;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.template.TemplateResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Enables tablist functionality.
 */
public class TablistService {

    private static final @NonNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm a");
    private static final @NonNull SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final @NonNull SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm a");

    private final @NonNull BatVelocityPlugin plugin;
    private final @NonNull MiniMessage miniMessage;
    private final @NonNull ConfigLoader configLoader;
    private final @NonNull ProxyServer server;
    private final @NonNull Logger logger;
    private final @NonNull Map<String, Tablist> tablistMap;
    private final @NonNull Map<String, ServerPing> pingMap;
    private final @NonNull LuckPerms luckPerms;

    private @MonotonicNonNull ScheduledTask pingUpdateTask;
    private @MonotonicNonNull ScheduledTask tablistUpdateTask;
    private @MonotonicNonNull PluginConfig config;


    /**
     * Constructs {@code TablistService}
     *
     * @param configLoader the config loader
     * @param server       the server list
     * @param logger       the logger
     */
    @Inject
    public TablistService(final @NonNull BatVelocityPlugin plugin,
                          final @NonNull ConfigLoader configLoader,
                          final @NonNull ProxyServer server,
                          final @NonNull Logger logger) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.tablistMap = new ConcurrentHashMap<>();
        this.pingMap = new ConcurrentHashMap<>();
        this.logger = logger;
        this.server = server;
        this.miniMessage = MiniMessage.miniMessage();
        this.luckPerms = LuckPermsProvider.get();
    }

    /**
     * Enables the tablist service.
     */
    public void enable() {
        this.config = configLoader.batConfig();
        for (final var entry : this.config.tablists.entrySet()) {
            final String id = entry.getKey();
            final TablistConfig tablistConfig = entry.getValue();
            final Tablist tablist = new Tablist(
                    tablistConfig.headerFormatStrings,
                    tablistConfig.footerFormatStrings,
                    tablistConfig.sortType
            );
            this.tablistMap.put(id, tablist);
        }

        this.pingUpdateTask = server.getScheduler()
                .buildTask(this.plugin, this::pingServers)
                .repeat(5L, TimeUnit.SECONDS)
                .schedule();

        this.tablistUpdateTask = server.getScheduler()
                .buildTask(this.plugin, this::updateTablists)
                .repeat(this.config.updateFrequency, TimeUnit.MILLISECONDS)
                .schedule();

    }

    /**
     * Disables the tablist service
     */
    public void disable() {
        this.tablistUpdateTask.cancel();
        this.pingUpdateTask.cancel();
        this.tablistMap.clear();
    }

    /**
     * Updates the player's tablist by showing them the appropriate list, or
     * none if configuration says.
     *
     * @param player the player
     */
    public void updateTablist(final @NonNull Player player) {
        final @Nullable Tablist tablist = this.tablist(this.config.defaultTablist);

        if (tablist == null) {
            this.logger.warning("Tried to show " + player.getUsername() + " a null tablist: '" + this.config.defaultTablist + "'");
            return;
        }

        this.showTablist(player, tablist);
    }

    /**
     * Shows a tablist to the player.
     *
     * @param player  the player
     * @param tablist the tablist
     */
    public void showTablist(final @NonNull Player player, final @NonNull Tablist tablist) {
        this.showText(player, tablist);
        this.updatePlayers(player, tablist);
    }

    /**
     * Returns the tablist with the given id. Null if none exists.
     *
     * @param id the id
     * @return the tablist
     */
    public @Nullable Tablist tablist(final @NonNull String id) {
        return this.tablistMap.get(id);
    }

    /**
     * Generates placeholder templates for the provided player.
     *
     * @param player the player
     * @param server the server
     * @return the template list
     */
    private @NonNull List<Template> generateTemplates(final @NonNull Player player,
                                                      final @NonNull ProxyServer server) {

        final Optional<ServerConnection> connectionOpt = player.getCurrentServer();

        int maxPlayers = 0;
        int onlinePlayers = 0;
        @NonNull Component motd = Component.empty();

        if (connectionOpt.isPresent()) {
            final ServerConnection connection = connectionOpt.get();
            final String name = connection.getServerInfo().getName();

            maxPlayers = this.getMaxPlayers(name);
            onlinePlayers = this.getPlayers(name);
            motd = this.getMotd(name);
        }

        final Date now = new Date();

        final String groupCodeFormat = this.groupCode(player);
        final String serverCodeFormat = this.serverCode(player);

        return List.of(
                Template.template("groupcode", groupCodeFormat),
                Template.template("servercode", serverCodeFormat),
                Template.template("proxycount", Integer.toString(server.getPlayerCount())),
                Template.template("proxymax", Integer.toString(server.getConfiguration().getShowMaxPlayers())),
                Template.template("proxymotd", server.getConfiguration().getMotd()),
                Template.template("servercount", Integer.toString(onlinePlayers)),
                Template.template("servermax", Integer.toString(maxPlayers)),
                Template.template("servermotd", motd),
                Template.template("playerping", Long.toString(player.getPing())),
                Template.template("playeruuid", player.getUniqueId().toString()),
                Template.template("playername", player.getUsername()),
                Template.template("playerip", player.getRemoteAddress().getAddress().getHostAddress()),
                Template.template("time", TIME_FORMAT.format(now)),
                Template.template("date", DATE_FORMAT.format(now)),
                Template.template("datetime", DATETIME_FORMAT.format(now))
        );
    }

    /**
     * Pings all servers and stores the ping information in a map.
     */
    private void pingServers() {
        final var servers = this.server.getAllServers();

        for (final RegisteredServer server : servers) {
            server.ping().thenAcceptAsync(ping -> this.pingMap.put(server.getServerInfo().getName(), ping));
        }
    }

    /**
     * Returns the MOTD from the server.
     *
     * @param serverName the server name
     * @return the name
     */
    private @NonNull Component getMotd(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            return this.pingMap.get(serverName).getDescriptionComponent();
        }

        return Component.text("");
    }

    /**
     * Returns {@code serverName}'s max players.
     *
     * @param serverName the server name
     * @return the max players
     */
    private int getMaxPlayers(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            final Optional<ServerPing.Players> players = this.pingMap.get(serverName).getPlayers();

            if (players.isPresent()) {
                return players.get().getMax();
            }
        }

        return 0;
    }


    /**
     * Returns {@code serverName}'s online players.
     *
     * @param serverName the server name
     * @return the online players
     */
    private int getPlayers(final @NonNull String serverName) {
        if (this.pingMap.containsKey(serverName)) {
            final Optional<ServerPing.Players> players = this.pingMap.get(serverName).getPlayers();

            if (players.isPresent()) {
                return players.get().getOnline();
            }
        }

        return 0;
    }

    /**
     * Updates tablists for every player on the proxy.
     */
    private void updateTablists() {
        for (final Player player : this.server.getAllPlayers()) {
            this.updateTablist(player);
        }
    }

    /**
     * Shows the tablist's text to the player.
     *
     * @param player  the player
     * @param tablist the tablist
     */
    private void showText(final @NonNull Player player,
                          final @NonNull Tablist tablist) {
        final List<Template> templates = this.generateTemplates(player, this.server);

        final List<String> footerFormatStrings = tablist.footerFormatStrings();
        final TextComponent.Builder footer = Component.text();

        final Iterator<String> footerIt = footerFormatStrings.iterator();

        while (footerIt.hasNext()) {
            footer.append(this.miniMessage.deserialize(footerIt.next(), TemplateResolver.templates(templates)));

            if (footerIt.hasNext()) {
                footer.append(Component.newline());
            }
        }

        final List<String> headerFormatStrings = tablist.headerFormatStrings();
        final TextComponent.Builder header = Component.text();

        final Iterator<String> headerIt = headerFormatStrings.iterator();

        while (headerIt.hasNext()) {
            header.append(this.miniMessage.deserialize(headerIt.next(), TemplateResolver.templates(templates)));

            if (headerIt.hasNext()) {
                header.append(Component.newline());
            }
        }

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    /**
     * Updates the names of players in the tablist.
     *
     * @param player  the player
     * @param tablist the tablist
     */
    private void updatePlayers(final @NonNull Player player,
                               final @NonNull Tablist tablist) {
        final Collection<TabListEntry> entries = player.getTabList().getEntries();

        for (TabListEntry entry : entries) {
            final UUID uuid = entry.getProfile().getId();
            final Optional<Player> otherPlayerOpt = this.server.getPlayer(uuid);

            if (otherPlayerOpt.isPresent()) {
                final Player otherPlayer = otherPlayerOpt.get();

                final TemplateResolver resolver = TemplateResolver.templates(this.generateTemplates(otherPlayer, this.server));
                final String groupCode = (String) resolver.resolve("groupcode").value();
                final String serverCode = (String) resolver.resolve("servercode").value();

                final String format = this.config.playerNameFormat
                        .replace("<groupcode>", groupCode)
                        .replace("<servercode>", serverCode);

                entry.setDisplayName(this.miniMessage.deserialize(format, resolver));
            }
        }
    }

    /**
     * Returns the MiniMessage groupCode string for the player.
     *
     * @param player the player
     * @return the group code (will return "" if there is no groupCode for the player)
     */
    private @NonNull String groupCode(final @NonNull Player player) {
        final Map<String, String> groupCodes = this.config.groupCodes;

        final User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return "";
        }

        final String group = this.luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup();

        return groupCodes.getOrDefault(group, "");
    }

    /**
     * Returns the MiniMessage serverCode string for the player.
     *
     * @param player the player
     * @return the server code (will return "" if there is no serverCode for the player)
     */
    private @NonNull String serverCode(final @NonNull Player player) {
        final Map<String, String> serverCodes = this.config.serverCodes;

        final Optional<ServerConnection> opt = player.getCurrentServer();
        if (opt.isEmpty()) {
            return "";
        }

        final ServerConnection serverConnection = opt.get();
        final String serverName = serverConnection.getServerInfo().getName();

        return serverCodes.getOrDefault(serverName, "");
    }

}
