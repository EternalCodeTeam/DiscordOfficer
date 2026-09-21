package com.eternalcode.discordapp.feature.releasewatch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.sentry.Sentry;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReleaseWatchGitHubClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseWatchGitHubClient.class);

    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN = "token ";
    private static final String USER_AGENT = "EternalCode-DiscordOfficer/1.0";
    private static final String ACCEPT = "application/vnd.github.v3+json";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    private ReleaseWatchGitHubClient() {}

    public static boolean repositoryExists(GitHubRepositoryRef repository, String githubToken) {
        Request request = newRequestBuilder(repository.toApiRepositoryUrl(), githubToken).build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            return response.isSuccessful();
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            LOGGER.warn("Failed to check if repository exists: {}", repository, exception);
            return false;
        }
    }

    public static Optional<GitHubRelease> getLatestRelease(GitHubRepositoryRef repository, String githubToken) {
        return fetchRelease(repository.toApiLatestReleaseUrl(), repository, githubToken);
    }

    public static Optional<GitHubRelease> getReleaseByTag(
        GitHubRepositoryRef repository,
        String githubToken,
        String tag
    ) {
        return fetchRelease(repository.toApiReleaseByTagUrl(tag), repository, githubToken);
    }

    private static Optional<GitHubRelease> fetchRelease(
        String url,
        GitHubRepositoryRef repository,
        String githubToken
    ) {
        Request request = newRequestBuilder(url, githubToken).build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.code() == 404) {
                return Optional.empty();
            }

            if (!response.isSuccessful()) {
                LOGGER.warn("GitHub API error {} while fetching release for {}", response.code(), repository);
                return Optional.empty();
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                return Optional.empty();
            }

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return Optional.of(toRelease(json));
        }
        catch (IOException | JsonSyntaxException exception) {
            Sentry.captureException(exception);
            LOGGER.warn("Failed to fetch release for {}", repository, exception);
            return Optional.empty();
        }
    }

    private static GitHubRelease toRelease(JsonObject json) {
        String tagName = json.has("tag_name") ? json.get("tag_name").getAsString() : "";
        String name = json.has("name") && !json.get("name").isJsonNull()
            ? json.get("name").getAsString()
            : tagName;
        String htmlUrl = json.has("html_url") ? json.get("html_url").getAsString() : "";
        String body = json.has("body") && !json.get("body").isJsonNull() ? json.get("body").getAsString() : "";
        Instant publishedAt = json.has("published_at") && !json.get("published_at").isJsonNull()
            ? Instant.parse(json.get("published_at").getAsString())
            : Instant.now();

        return new GitHubRelease(tagName, name, htmlUrl, body, publishedAt);
    }

    private static Request.Builder newRequestBuilder(String url, String githubToken) {
        Request.Builder builder = new Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", ACCEPT);

        if (githubToken != null && !githubToken.isBlank()) {
            builder.header(AUTHORIZATION, TOKEN + githubToken);
        }

        return builder;
    }
}
