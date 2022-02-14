package com.nearvanilla.bat.velocity.tab;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameProfileSorter {
    
    private final Map<String, Integer> groupPriorities;
    private final Map<String, Integer> serverPriorities;
    private final LuckPerms luckPerms;
    private final ProxyServer proxyServer;

    public GameProfileSorter(Map<String, Integer> groupPriorities, Map<String, Integer> serverPriorities, ProxyServer proxyServer) {
        this.groupPriorities = groupPriorities;
        this.serverPriorities = serverPriorities;
        this.proxyServer = proxyServer;
        this.luckPerms = LuckPermsProvider.get();
    }
    
    public GameProfileSorter(List<String> groupPriorities, List<String> serverPriorities, ProxyServer proxyServer) {
        this(listToMap(groupPriorities), listToMap(serverPriorities), proxyServer);
    }

    private static Map<String, Integer> listToMap(List<String> list) {
        final int total = list.size();
        final Iterator<String> iterator = list.iterator();
        final Map<String, Integer> result = new HashMap<>();
        for (int ordinal = 0; iterator.hasNext(); ++ordinal) {
            result.put(iterator.next(), total - ordinal);
        }
        return result;
    }
    
    public Function<Collection<GameProfile>, Collection<GameProfile>> getSorter(SortType sortType) {
        return switch (sortType) {
            case NONE -> Function.identity();
            case GROUP -> keyedSorter(this::groupPositioner);
            case SERVER -> keyedSorter(this::serverPositioner);
        };
    }
    
    public Collection<GameProfile> sorted(Collection<GameProfile> toSort, SortType sortType) {
        return this.getSorter(sortType).apply(toSort);
    }

    private static Function<Collection<GameProfile>, Collection<GameProfile>> keyedSorter(Function<GameProfile, Integer> positioner) {
        return (collection) -> collection
                .stream()
                .map((entry) -> new GameProfileKey(positioner.apply(entry), entry))
                .sorted()
                .map(GameProfileKey::getProfile)
                .collect(Collectors.toList());
    }
    
    private int groupPositioner(GameProfile profile) {
        @Nullable User user = this.luckPerms.getUserManager().getUser(profile.getId());
        if (user == null) { // Should never happen
            return 0;
        }
        return user.getInheritedGroups(Const.LP_QUERY_RECURSIVE)
                .stream()
                .map(Group::getName)
                .map(gname -> groupPriorities.getOrDefault(gname, 0))
                .max(Integer::compare)
                .orElse(0);
    }

    private int serverPositioner(GameProfile profile) {
        String serverName = this.proxyServer.getPlayer(profile.getId()).flatMap(player -> player.getCurrentServer().map(serverConnection -> serverConnection.getServerInfo().getName())).orElse("");
        return this.serverPriorities.getOrDefault(serverName, 0);
    }


    private record GameProfileKey(int position,
                                  GameProfile profile) implements Comparable<GameProfileKey> {
        private final static Comparator<GameProfileKey> comparator = Comparator.comparing(GameProfileKey::getPosition).thenComparing((x) -> x.profile.getName());

        public int getPosition() {
            return position;
        }

        public GameProfile getProfile() {
            return profile;
        }

        @Override
        public int compareTo(@NotNull GameProfileKey other) {
            return comparator.compare(this, other);
        }
    }

    public enum SortType {
        SERVER,
        GROUP,
        NONE;
    }
}
