package com.eternalcode.discordapp.feature.releasewatch;

public enum ReleaseLinkPlatform {

    MODRINTH("Modrinth"),
    HANGAR("Hangar"),
    SPIGOTMC("SpigotMC"),
    CUSTOM("Custom");

    private final String displayName;

    ReleaseLinkPlatform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
