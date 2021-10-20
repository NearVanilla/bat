package com.nearvanilla.bat.velocity.tab;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class Tablist {

    private final @NonNull List<String> headerFormatStrings;
    private final @NonNull List<String> footerFormatStrings;
    private final @NonNull SortType sortType;

    /**
     * Constructs {@code Tablist}.
     *
     * @param headerFormatStrings a list containing the tablist's header
     * @param footerFormatStrings a list containing the tablist's footer
     * @param sortType            the tablist's sorting type
     */
    public Tablist(final @NonNull List<String> headerFormatStrings,
                   final @NonNull List<String> footerFormatStrings,
                   final @NonNull SortType sortType) {
        this.headerFormatStrings = headerFormatStrings;
        this.footerFormatStrings = footerFormatStrings;
        this.sortType = sortType;
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
