package com.nearvanilla.bat.velocity.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nearvanilla.bat.velocity.BatVelocityPlugin;
import com.nearvanilla.bat.velocity.config.ConfigLoader;
import com.nearvanilla.bat.velocity.config.PluginConfig;
import com.nearvanilla.bat.velocity.config.TablistConfig;
import com.nearvanilla.bat.velocity.tab.group.LuckPermsGroupMeta;
import com.nearvanilla.bat.velocity.tab.group.LuckPermsGroupProvider;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Enables tablist functionality.
 */
@Singleton
public class TablistService {

    private static final @NonNull SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a");
    private static final @NonNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final @NonNull SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm a");

    private final @NonNull BatVelocityPlugin plugin;
    private final @NonNull MiniMessage miniMessage;
    private final @NonNull ConfigLoader configLoader;
    private final @NonNull ProxyServer server;
    private final @NonNull Logger logger;
    private final @NonNull ServerDataProvider serverDataProvider;

    private final @NonNull Map<String, Tablist> tablistMap;
    private @MonotonicNonNull Tablist defaultTablist;

    private @MonotonicNonNull ScheduledTask tablistUpdateTask;
    private @MonotonicNonNull PluginConfig config;


    /**
     * Constructs {@code TablistService}
     *
     * @param plugin             the plugin
     * @param configLoader       the config loader
     * @param serverDataProvider the server data provider
     * @param server             the server list
     * @param logger             the logger
     */
    @Inject
    public TablistService(final @NonNull BatVelocityPlugin plugin,
                          final @NonNull ConfigLoader configLoader,
                          final @NonNull ServerDataProvider serverDataProvider,
                          final @NonNull ProxyServer server,
                          final @NonNull Logger logger) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.serverDataProvider = serverDataProvider;
        this.logger = logger;
        this.server = server;
        this.miniMessage = MiniMessage.miniMessage();
        this.tablistMap = new ConcurrentHashMap<>();
    }

    /**
     * Enables the tablist service.
     */
    public void enable() {
        if (this.defaultTablist != null) {
            for (final Player player : this.server.getAllPlayers()) {
                this.defaultTablist.removePlayer(player);
            }
        }

        this.tablistMap.clear();

        this.config = configLoader.batConfig();
        for (final var entry : this.config.tablists.entrySet()) {
            final String id = entry.getKey();
            final TablistConfig tablistConfig = entry.getValue();
            final Tablist tablist = new Tablist(
                    this.logger, this, this.serverDataProvider,
                    tablistConfig.headerFormatStrings,
                    tablistConfig.footerFormatStrings
            );
            this.tablistMap.put(id, tablist);
        }

        this.defaultTablist = this.tablistMap.get(this.config.defaultTablist);

        if (this.tablistUpdateTask != null) {
            this.tablistUpdateTask.cancel();
        }

        this.tablistUpdateTask = server.getScheduler()
                .buildTask(this.plugin, this::updateTablists)
                .repeat(this.config.updateFrequency, TimeUnit.MILLISECONDS)
                .schedule();

        if (this.defaultTablist != null) {
            for (final Player player : this.server.getAllPlayers()) {
                this.defaultTablist.addPlayer(player);
            }
        }
    }

    /**
     * Removes the player from the tablist.
     *
     * @param player the player
     */
    public void handlePlayerLeave(final @NonNull Player player) {
        this.defaultTablist.removePlayer(player);
    }


    /**
     * Updates a player's tablist info with their new server connection.
     *
     * @param player the player
     */
    public void handleServerConnection(final @NonNull Player player) {
        this.defaultTablist.removePlayer(player);
        this.defaultTablist.addPlayer(player);
    }


    /**
     * Returns a player's display name as a component.
     *
     * @param uuid the uuid of the player
     * @return the display name
     */
    protected @NonNull Component displayName(final @NonNull UUID uuid) {
        final Optional<Player> opt = this.server.getPlayer(uuid);

        if (opt.isEmpty()) {
            return Component.empty();
        }

        final Player player = opt.get();
        final TagResolver tagResolver = this.tagResolver(player);


        MineDown mineDown;
        if (serverDataProvider.groupProvider() instanceof LuckPermsGroupProvider luckPermsGroupProvider) {
            LuckPermsGroupMeta highestWeightGroupMeta = luckPermsGroupProvider.getHighestWeightGroup(uuid);

            String mineDownString = MineDown.stringify(this.miniMessage.deserialize(
                    highestWeightGroupMeta.getMetaData().getPrefix() + " <playername>", tagResolver));
            mineDown = new MineDown(mineDownString);
        } else {
            String mineDownString = MineDown.stringify(this.miniMessage.deserialize(this.config.playerNameFormat, tagResolver));
            mineDown = new MineDown(mineDownString);
        }

        return mineDown.toComponent();
    }

    /**
     * Updates a player's tablist's header and footer.
     *
     * @param player the player
     */
    public void updateText(final @NonNull Player player) {
        final Tablist tablist = this.defaultTablist;

        if (tablist == null) {
            this.logger.warning("Tried to show " + player.getUsername() + " a null tablist: '" + this.config.defaultTablist + "'");
            return;
        }

        final TagResolver tagResolver = this.tagResolver(player);

        final List<String> footerFormatStrings = tablist.footerFormatStrings();
        final TextComponent.Builder footer = Component.text();

        final Iterator<String> footerIt = footerFormatStrings.iterator();

        while (footerIt.hasNext()) {
            String mineDownString = MineDown.stringify(this.miniMessage.deserialize(footerIt.next(), tagResolver));
            MineDown mineDown = new MineDown(mineDownString);
            footer.append(mineDown.toComponent());

            if (footerIt.hasNext()) {
                footer.append(Component.newline());
            }
        }

        final List<String> headerFormatStrings = tablist.headerFormatStrings();
        final TextComponent.Builder header = Component.text();

        final Iterator<String> headerIt = headerFormatStrings.iterator();

        while (headerIt.hasNext()) {
            String mineDownString = MineDown.stringify(this.miniMessage.deserialize(headerIt.next(), tagResolver));
            MineDown mineDown = new MineDown(mineDownString);
            header.append(mineDown.toComponent());

            if (headerIt.hasNext()) {
                header.append(Component.newline());
            }
        }

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    /**
     * Generates placeholder tagResolver for the provided player.
     *
     * @param player the player
     * @return the template list
     */
    private @NonNull TagResolver tagResolver(final @NonNull Player player) {
        final Optional<ServerConnection> connectionOpt = player.getCurrentServer();

        int maxPlayers = 0;
        int onlinePlayers = 0;
        @NonNull Component motd = Component.empty();
        @NonNull String serverName = "";

        if (connectionOpt.isPresent()) {
            final ServerConnection connection = connectionOpt.get();
            final String name = connection.getServerInfo().getName();

            maxPlayers = this.serverDataProvider.getMaxPlayers(name);
            onlinePlayers = this.serverDataProvider.getPlayers(name);
            motd = this.serverDataProvider.getMotd(name);
            serverName = name;
        }

        final Date now = new Date();

        final String groupCodeFormat = this.groupCode(player);
        final String serverCodeFormat = this.serverCode(player);

        return TagResolver.resolver(List.of(
                Placeholder.parsed("groupcode", groupCodeFormat),
                Placeholder.parsed("servercode", serverCodeFormat),
                Placeholder.unparsed("proxycount", Integer.toString(server.getPlayerCount())),
                Placeholder.unparsed("proxymax", Integer.toString(server.getConfiguration().getShowMaxPlayers())),
                Placeholder.component("proxymotd", server.getConfiguration().getMotd()),
                Placeholder.unparsed("servercount", Integer.toString(onlinePlayers)),
                Placeholder.unparsed("servermax", Integer.toString(maxPlayers)),
                Placeholder.component("servermotd", motd),
                Placeholder.unparsed("playerping", Long.toString(player.getPing())),
                Placeholder.unparsed("playeruuid", player.getUniqueId().toString()),
                Placeholder.unparsed("playername", player.getUsername()),
                Placeholder.unparsed("playerip", player.getRemoteAddress().getAddress().getHostAddress()),
                Placeholder.unparsed("playerserver", serverName),
                Placeholder.unparsed("time", TIME_FORMAT.format(now)),
                Placeholder.unparsed("date", DATE_FORMAT.format(now)),
                Placeholder.unparsed("datetime", DATETIME_FORMAT.format(now))
        ));
    }

    /**
     * Returns the MiniMessage groupCode string for the player.
     *
     * @param player the player
     * @return the group code (will return "" if there is no groupCode for the player)
     */
    private @NonNull String groupCode(final @NonNull Player player) {
        final Map<String, String> groupCodes = this.config.groupCodes;

        String result = "";
        for (final String group : this.serverDataProvider.groupProvider().groups(player.getUniqueId())) {
            result = groupCodes.getOrDefault(group, "");
            if (!"".equals(result)) {
                return result;
            }
        }
        return result;
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

    /**
     * Updates every player's tablist on the network.
     */
    private void updateTablists() {
        for (final Player player : this.server.getAllPlayers()) {
            this.updateText(player);

            final TabList tabList = player.getTabList();

            final List<TabListEntry> currentEntries = this.defaultTablist.entries(tabList);
            final List<TabListEntry> entries = new ArrayList<>(tabList.getEntries());

            boolean equals = currentEntries.size() == entries.size();

            if (equals) {
                for (int i = 0; i < currentEntries.size(); i++) {
                    final TabListEntry currentEntry = currentEntries.get(i);
                    final TabListEntry playerEntry = entries.get(i);

                    final Component currentDisplayName = currentEntry.getDisplayNameComponent().isPresent()
                            ? currentEntry.getDisplayNameComponent().get()
                            : Component.empty();

                    final Component playerDisplayName = playerEntry.getDisplayNameComponent().isPresent()
                            ? playerEntry.getDisplayNameComponent().get()
                            : Component.empty();

                    if (!playerDisplayName.equals(currentDisplayName)) {
                        equals = false;
                        break;
                    }
                }
            }

            if (!equals) {
                final TabList newTabList = player.getTabList();
                final List<TabListEntry> newEntries = this.defaultTablist.entries(newTabList);

                for (final TabListEntry entry : newTabList.getEntries()) {
                    newTabList.removeEntry(entry.getProfile().getId());
                }

                for (final TabListEntry currentEntry : newEntries) {
                    newTabList.addEntry(currentEntry);
                }
            }
        }
    }

    public int ping(final @NonNull UUID uuid) {
        final Optional<Player> player = server.getPlayer(uuid);
        return player.map(Player::getPing).orElse(-1L).intValue();
    }

}
