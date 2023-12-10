package com.eternalcode.discordapp.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class InstantFormatUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofPattern("dd.MM.yyyy HH:mm", Locale.ROOT)
        .withZone(ZoneId.systemDefault());

    private InstantFormatUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String format(Instant instant) {
        return DATE_FORMAT.format(instant);
    }
}
