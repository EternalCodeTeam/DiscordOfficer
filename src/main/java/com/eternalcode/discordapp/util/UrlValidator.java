package com.eternalcode.discordapp.util;

import java.util.regex.Pattern;

public final class UrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[\\w\\-.]+(:\\d+)?(/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    private UrlValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValid(String url) {
        if (url == null) {
            return false;
        }

        String trimmedUrl = url.trim();
        if (trimmedUrl.isEmpty()) {
            return false;
        }

        return URL_PATTERN.matcher(trimmedUrl).matches();
    }
}
