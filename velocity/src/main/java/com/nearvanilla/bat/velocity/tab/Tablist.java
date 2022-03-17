package com.nearvanilla.bat.velocity.tab;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Tablist {

    private final @NonNull TablistService tablistService;
    private final @NonNull ServerDataProvider serverDataProvider;
    private final @NonNull List<String> headerFormatStrings;
    private final @NonNull List<String> footerFormatStrings;
    private final @NonNull SortType sortType;
    private final @NonNull List<GameProfile> profileEntries;
    private final @NonNull List<String> serverSortPriorities;
    private final @NonNull List<String> groupSortPriorities;

    /**
     * Constructs {@code Tablist}.
     *
     * @param tablistService      the tablist service
     * @param serverDataProvider  the server data provider
     * @param headerFormatStrings a list containing the tablist's header
     * @param footerFormatStrings a list containing the tablist's footer
     * @param sortType            the tablist's sorting type
     */
    public Tablist(final @NonNull TablistService tablistService,
                   final @NonNull ServerDataProvider serverDataProvider,
                   final @NonNull List<String> headerFormatStrings,
                   final @NonNull List<String> footerFormatStrings,
                   final @NonNull SortType sortType,
                   final @NonNull List<String> serverSortPriorities,
                   final @NonNull List<String> groupSortPriorities) {
        this.tablistService = tablistService;
        this.serverDataProvider = serverDataProvider;
        this.headerFormatStrings = headerFormatStrings;
        this.footerFormatStrings = footerFormatStrings;
        this.serverSortPriorities = serverSortPriorities;
        this.groupSortPriorities = groupSortPriorities;

        this.sortType = sortType;
        this.profileEntries = new ArrayList<>();
    }

    public void addPlayer(final @NonNull Player player) {
        this.insert(player.getGameProfile());
    }

    public void removePlayer(final @NonNull Player player) {
        this.profileEntries.removeIf(profile -> profile.getId().equals(player.getUniqueId()));
    }

    /**
     * Generates {@link TabListEntry} for the tablist.
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
                    .gameMode(0)
                    .build()
            ).toList();
    }

    private void insert(final @NonNull GameProfile gameProfile) {
        this.profileEntries.add(gameProfile);
    }

    public @NonNull List<String> headerFormatStrings() {
        return this.headerFormatStrings;
    }

    public @NonNull List<String> footerFormatStrings() {
        return this.footerFormatStrings;
    }

    public @NonNull SortType sortType() {
        return this.sortType;
    }
}
