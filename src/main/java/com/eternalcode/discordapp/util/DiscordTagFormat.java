package com.eternalcode.discordapp.util;

import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;

public final class DiscordTagFormat {

    private static final String MEMBER_TAG = "<@%s>";
    private static final String TIMESTAMP_TAG = "<t:%d:F>";

    private DiscordTagFormat() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static String memberTag(User user) {
        return String.format(MEMBER_TAG, user.getId());
    }

    public static String offsetTime(OffsetDateTime offsetDateTime) {
        return String.format(TIMESTAMP_TAG, offsetDateTime.toEpochSecond());
    }

}
