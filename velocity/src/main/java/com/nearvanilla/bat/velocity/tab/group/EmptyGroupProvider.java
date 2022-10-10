package com.nearvanilla.bat.velocity.tab.group;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * {@code EmptyGroupProvider} returns an empty {@link Collection} when queried.
 */
public class EmptyGroupProvider implements GroupProvider {

    @Override
    public @NonNull Collection<String> groups(final @NonNull UUID uuid) {
        return List.of();
    }

}
