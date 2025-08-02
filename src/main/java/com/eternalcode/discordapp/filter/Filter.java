package com.eternalcode.discordapp.filter;

@FunctionalInterface
public interface Filter {
    FilterResult filter(String... sources);
}
