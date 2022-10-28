package com.nearvanilla.bat.velocity.tab;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Tablist {

    private final @NonNull Logger logger;
    private final @NonNull TablistService tablistService;
    private final @NonNull ServerDataProvider serverDataProvider;
    private final @NonNull List<String> headerFormatStrings;
    private final @NonNull List<String> footerFormatStrings;
    private final @NonNull List<GameProfile> profileEntries;

    /**
     * Constructs {@code Tablist}.
     *
     * @param logger               the logger
     * @param tablistService       the tablist service
     * @param serverDataProvider   the server data provider
     * @param headerFormatStrings  a list containing the tablist's header
     * @param footerFormatStrings  a list containing the tablist's footer
     */
    public Tablist(final @NonNull Logger logger,
                   final @NonNull TablistService tablistService,
                   final @NonNull ServerDataProvider serverDataProvider,
                   final @NonNull List<String> headerFormatStrings,
                   final @NonNull List<String> footerFormatStrings) {
        this.logger = logger;
        this.tablistService = tablistService;
        this.serverDataProvider = serverDataProvider;
        this.headerFormatStrings = headerFormatStrings;
        this.footerFormatStrings = footerFormatStrings;
        this.profileEntries = new ArrayList<>();
    }

    /**
     * Adds a player to the tablist.
     *
     * @param player the player
     */
    public void addPlayer(final @NonNull Player player) {
        this.profileEntries.add(player.getGameProfile());
    }

    /**
     * Removes the player from the tablist.
     *
     * @param player the player
     */
    public void removePlayer(final @NonNull Player player) {
        this.profileEntries.removeIf(profile -> profile.getId().equals(player.getUniqueId()));
    }

    /**
     * Generates a list of {@link TabListEntry}s for the tablist.
     *
     * @param tabList the tablist
     * @return the list of tablist entries
     */
    public @NonNull List<TabListEntry> entries(final @NonNull TabList tabList) {
        return profileEntries
                .stream()
                .sorted(Comparator.comparing(GameProfile::getName))
                .map(gameProfile ->
                        TabListEntry.builder()
                                .latency(10)
                                .tabList(tabList)
                                .profile(gameProfile)
                                .displayName(this.tablistService.displayName(gameProfile.getId()))
                                .gameMode(this.getGameMode(tabList, gameProfile.getId()))
                                .build()
                ).toList();
    }

    public @NonNull List<String> headerFormatStrings() {
        return this.headerFormatStrings;
    }

    public @NonNull List<String> footerFormatStrings() {
        return this.footerFormatStrings;
    }

    private int getGameMode(final @NonNull TabList tabList,
                            final @NonNull UUID uuid) {
        for (final TabListEntry entry : tabList.getEntries()) {
            if (entry.getProfile().getId().equals(uuid)) {
                return entry.getGameMode();
            }
        }

        this.logger.warning(String.format("Failed to determine GameMode for %s! Returning: 0", uuid));

        return 0;
    }

}
