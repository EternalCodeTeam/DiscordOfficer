package com.eternalcode.discordapp.feature.filter;

@FunctionalInterface
public interface Filter {
    FilterResult filter(String... sources);
}

