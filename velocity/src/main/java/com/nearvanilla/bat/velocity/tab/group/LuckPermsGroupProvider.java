package com.nearvanilla.bat.velocity.tab.group;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * {@code LuckPermsGroupProvider} utilizes the {@link LuckPerms} API to retrieve a user's group information.
 */
public class LuckPermsGroupProvider implements GroupProvider {

    private final @NonNull LuckPerms luckPerms;

    /**
     * Constructs {@code LuckPermsGroupProvider}.
     *
     * @param luckPerms the {@link LuckPerms} API instance
     */
    public LuckPermsGroupProvider(final @NonNull LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    /**
     * Returns a player's groups.
     *
     * @param uuid the uuid
     * @return the collection of group names
     */
    @Override
    public @NonNull Collection<String> groups(final @NonNull UUID uuid) {
        final User user = this.luckPerms.getUserManager().getUser(uuid);

        if (user == null) {
            return new ArrayList<>();
        }

        return user.getInheritedGroups(QueryOptions.defaultContextualOptions().toBuilder().flag(Flag.RESOLVE_INHERITANCE, true).build())
                .stream()
                .map(Group::getName)
                .toList();
    }


}
