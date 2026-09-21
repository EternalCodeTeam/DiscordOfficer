package com.eternalcode.discordapp.feature.releasewatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModrinthClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModrinthClient.class);

    private static final String USER_AGENT = "EternalCode-DiscordOfficer/1.0";
    private static final String API_PROJECT_VERSIONS_URL = "https://api.modrinth.com/v2/project/%s/version";
    private static final String PROJECT_URL = "https://modrinth.com/project/%s";
    private static final String PROJECT_VERSION_URL = "https://modrinth.com/project/%s/version/%s";
    private static final String PROJECT_VERSIONS_LIST_URL = "https://modrinth.com/project/%s/versions";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    private ModrinthClient() {}

    public static String extractProjectSlug(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return trimmed;
        }

        String withoutQuery = trimmed.split("[?#]")[0];
        String[] segments = withoutQuery.split("/");
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isBlank() && !"version".equalsIgnoreCase(segments[i])) {
                return segments[i];
            }
        }

        return trimmed;
    }

    public static String resolveVersionUrl(String projectSlug, String releaseTag) {
        String slug = extractProjectSlug(projectSlug);
        String url = String.format(API_PROJECT_VERSIONS_URL, slug);

        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.warn("Modrinth API error {} while resolving version for {}", response.code(), slug);
                return String.format(PROJECT_URL, slug);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                return String.format(PROJECT_URL, slug);
            }

            JsonArray versions = JsonParser.parseString(responseBody).getAsJsonArray();
            String normalizedTag = normalize(releaseTag);

            for (int i = 0; i < versions.size(); i++) {
                JsonObject version = versions.get(i).getAsJsonObject();
                String versionNumber = version.has("version_number") && !version.get("version_number").isJsonNull()
                    ? version.get("version_number").getAsString()
                    : "";

                if (!versionNumber.isBlank() && normalize(versionNumber).equals(normalizedTag)) {
                    String versionId = version.get("id").getAsString();
                    return String.format(PROJECT_VERSION_URL, slug, versionId);
                }
            }

            LOGGER.info("No matching Modrinth version found for {} (tag {}), falling back to versions list", slug, releaseTag);
            return String.format(PROJECT_VERSIONS_LIST_URL, slug);
        }
        catch (IOException | JsonSyntaxException exception) {
            Sentry.captureException(exception);
            LOGGER.warn("Failed to resolve Modrinth version for {}", slug, exception);
            return String.format(PROJECT_URL, slug);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if ((trimmed.startsWith("v") || trimmed.startsWith("V")) && trimmed.length() > 1 && Character.isDigit(trimmed.charAt(1))) {
            trimmed = trimmed.substring(1);
        }

        return trimmed.toLowerCase();
    }
}
