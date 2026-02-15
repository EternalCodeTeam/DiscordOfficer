package com.eternalcode.discordapp.feature.review;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class GitHubReviewUtil {

    private static final Logger LOGGER = Logger.getLogger(GitHubReviewUtil.class.getName());

    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";
    private static final int MAX_TITLE_LENGTH = 100;

    private static final Gson GSON = new Gson();
    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN = "token ";
    private static final String HTTP_ERROR = "HTTP Error: ";
    private static final String USER_AGENT = "EternalCode-DiscordBot/1.0";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(new GitHubRetryInterceptor())
        .addInterceptor(new GitHubRateLimitInterceptor())
        .build();

    private GitHubReviewUtil() {}

    public static boolean isPullRequestTitleValid(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

    public static boolean isTitleLengthValid(String title) {
        if (title == null) {
            return false;
        }
        return title.length() <= MAX_TITLE_LENGTH;
    }

    public static List<String> getReviewers(GitHubPullRequest pullRequest, String githubToken) {
        if (pullRequest == null || githubToken == null || githubToken.trim().isEmpty()) {
            LOGGER.warning("Invalid parameters for getReviewers");
            return Collections.emptyList();
        }

        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl();
                if (response.code() == 404) {
                    LOGGER.warning("Pull request not found: " + pullRequest.toApiUrl());
                }
                else if (response.code() == 403) {
                    LOGGER.warning("GitHub API rate limit exceeded or access denied");
                }
                else {
                    LOGGER.warning(errorMsg);
                    Sentry.captureException(new IOException(errorMsg));
                }
                return Collections.emptyList();
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                LOGGER.warning("Empty response body from GitHub API");
                return Collections.emptyList();
            }

            JsonObject json;
            try {
                json = GSON.fromJson(responseBody, JsonObject.class);
            }
            catch (JsonSyntaxException exception) {
                LOGGER.log(Level.WARNING, "Invalid JSON response from GitHub API", exception);
                return Collections.emptyList();
            }

            if (json == null || !json.has("requested_reviewers")) {
                return Collections.emptyList();
            }

            JsonArray requestedReviewers = json.getAsJsonArray("requested_reviewers");
            List<String> reviewers = new ArrayList<>();

            for (int i = 0; i < requestedReviewers.size(); i++) {
                try {
                    JsonObject reviewer = requestedReviewers.get(i).getAsJsonObject();
                    if (reviewer != null && reviewer.has("login")) {
                        String reviewerLogin = reviewer.get("login").getAsString();
                        if (reviewerLogin != null && !reviewerLogin.trim().isEmpty()) {
                            reviewers.add(reviewerLogin);
                        }
                    }
                }
                catch (Exception exception) {
                    LOGGER.log(Level.WARNING, "Error parsing reviewer at index " + i, exception);
                }
            }

            return reviewers;
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "IOException in getReviewers for " + pullRequest.toApiUrl(), exception);
            return Collections.emptyList();
        }
    }

    public static String getPullRequestTitleFromUrl(GitHubPullRequest pullRequest, String githubToken)
        throws IOException {
        if (pullRequest == null || githubToken == null || githubToken.trim().isEmpty()) {
            throw new IOException("Invalid parameters for getPullRequestTitleFromUrl");
        }

        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                throw new IOException("Empty response body from GitHub API for URL: " + pullRequest.toApiUrl());
            }

            JsonObject jsonObject;
            try {
                jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            }
            catch (JsonSyntaxException exception) {
                throw new IOException("Invalid JSON response from GitHub API for URL: " + pullRequest.toApiUrl(), exception);
            }

            if (jsonObject == null || !jsonObject.has("title")) {
                throw new IOException("Missing 'title' in API response for URL: " + pullRequest.toApiUrl());
            }

            String title = jsonObject.get("title").getAsString();
            return title != null ? title : "";
        }
    }

    public static boolean isPullRequestMerged(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        return checkPullRequestBooleanField(pullRequest, githubToken, "merged");
    }

    public static boolean isPullRequestClosed(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        if (pullRequest == null || githubToken == null || githubToken.trim().isEmpty()) {
            throw new IOException("Invalid parameters for isPullRequestClosed");
        }

        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject json;
            try {
                json = GSON.fromJson(responseBody, JsonObject.class);
            }
            catch (JsonSyntaxException exception) {
                throw new IOException("Invalid JSON response from GitHub API", exception);
            }

            if (json == null || !json.has("state")) {
                throw new IOException("Missing 'state' in API response for URL: " + pullRequest.toApiUrl());
            }

            String state = json.get("state").getAsString();
            return "closed".equalsIgnoreCase(state);
        }
    }

    private static boolean checkPullRequestBooleanField(
        GitHubPullRequest pullRequest,
        String githubToken,
        String fieldName
    ) throws IOException {
        if (pullRequest == null || githubToken == null || githubToken.trim().isEmpty() || fieldName == null) {
            throw new IOException("Invalid parameters for checkPullRequestBooleanField");
        }

        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject json;
            try {
                json = GSON.fromJson(responseBody, JsonObject.class);
            }
            catch (JsonSyntaxException exception) {
                throw new IOException("Invalid JSON response from GitHub API", exception);
            }

            if (json == null || !json.has(fieldName)) {
                throw new IOException("Missing '" + fieldName + "' in API response for URL: " + pullRequest.toApiUrl());
            }
            return json.get(fieldName).getAsBoolean();
        }
    }

    public static boolean hasUserReviewed(GitHubPullRequest pullRequest, String githubToken, String githubUsername) {
        if (pullRequest == null || githubToken == null || githubToken.trim().isEmpty() ||
            githubUsername == null || githubUsername.trim().isEmpty()) {
            LOGGER.warning("Invalid parameters for hasUserReviewed");
            return false;
        }

        String reviewsUrl = pullRequest.toApiUrl() + "/reviews";
        Request request = new Request.Builder()
            .url(reviewsUrl)
            .header(AUTHORIZATION, TOKEN + githubToken)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/vnd.github.v3+json")
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = HTTP_ERROR + response.code() + " for URL: " + reviewsUrl;
                if (response.code() == 404) {
                    LOGGER.warning("Reviews not found for PR: " + pullRequest.toUrl());
                }
                else {
                    LOGGER.warning(errorMsg);
                    Sentry.captureException(new IOException(errorMsg));
                }
                return false;
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            if (responseBody.isEmpty()) {
                LOGGER.warning("Empty response body for reviews");
                return false;
            }

            JsonArray reviews;
            try {
                reviews = JsonParser.parseString(responseBody).getAsJsonArray();
            }
            catch (JsonSyntaxException exception) {
                LOGGER.log(Level.WARNING, "Invalid JSON response for reviews", exception);
                return false;
            }

            for (int i = 0; i < reviews.size(); i++) {
                try {
                    JsonObject review = reviews.get(i).getAsJsonObject();
                    if (review != null && review.has("user")) {
                        JsonObject user = review.getAsJsonObject("user");
                        if (user != null && user.has("login")) {
                            String login = user.get("login").getAsString();
                            if (githubUsername.equalsIgnoreCase(login)) {
                                if (review.has("state")) {
                                    String state = review.get("state").getAsString();
                                    if ("APPROVED".equalsIgnoreCase(state) ||
                                        "CHANGES_REQUESTED".equalsIgnoreCase(state) ||
                                        "COMMENTED".equalsIgnoreCase(state)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception exception) {
                    LOGGER.log(Level.WARNING, "Error parsing review at index " + i, exception);
                }
            }
            return false;
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "IOException in hasUserReviewed", exception);
            return false;
        }
    }
}

