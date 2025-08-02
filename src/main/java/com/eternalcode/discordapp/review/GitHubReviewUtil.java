package com.eternalcode.discordapp.review;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class GitHubReviewUtil {

    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";
    private static final int MAX_TITLE_LENGTH = 100;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final Gson GSON = new Gson();
    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN = "token ";
    private static final String HTTP_ERROR = "HTTP Error: ";

    private GitHubReviewUtil() {}

    public static boolean isPullRequestTitleValid(String title) {
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

    public static boolean isTitleLengthValid(String title) {
        return title.length() <= MAX_TITLE_LENGTH;
    }

    public static List<String> getReviewers(GitHubPullRequest pullRequest, String githubToken) {
        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Sentry.captureException(new IOException(
                    HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl()));
                return Collections.emptyList();
            }

            String responseBody = response.body().string();
            JsonObject json = GSON.fromJson(responseBody, JsonObject.class);

            if (json == null || !json.has("requested_reviewers")) {
                return Collections.emptyList();
            }

            JsonArray requestedReviewers = json.getAsJsonArray("requested_reviewers");

            List<String> reviewers = new ArrayList<>();
            for (int i = 0; i < requestedReviewers.size(); i++) {
                JsonObject reviewer = requestedReviewers.get(i).getAsJsonObject();
                if (reviewer != null && reviewer.has("login")) {
                    String reviewerLogin = reviewer.get("login").getAsString();
                    reviewers.add(reviewerLogin);
                }
            }

            return reviewers;
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            return Collections.emptyList();
        }
    }

    public static String getPullRequestTitleFromUrl(GitHubPullRequest pullRequest, String githubToken)
        throws IOException {
        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            String string = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();

            if (jsonObject == null || !jsonObject.has("title")) {
                throw new IOException("Missing 'title' in API response for URL: " + pullRequest.toApiUrl());
            }
            return jsonObject.get("title").getAsString();
        }
    }

    public static boolean isPullRequestMerged(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            JsonObject json = GSON.fromJson(response.body().string(), JsonObject.class);
            if (json == null || !json.has("merged")) {
                throw new IOException("Missing 'merged' in API response for URL: " + pullRequest.toApiUrl());
            }
            return json.get("merged").getAsBoolean();
        }
    }

    public static boolean isPullRequestClosed(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        Request request = new Request.Builder()
            .url(pullRequest.toApiUrl())
            .header(AUTHORIZATION, TOKEN + githubToken)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(HTTP_ERROR + response.code() + " for URL: " + pullRequest.toApiUrl());
            }

            JsonObject json = GSON.fromJson(response.body().string(), JsonObject.class);
            if (json == null || !json.has("state")) {
                throw new IOException("Missing 'state' in API response for URL: " + pullRequest.toApiUrl());
            }
            String state = json.get("state").getAsString();
            return "closed".equalsIgnoreCase(state);
        }
    }

    public static boolean hasUserReviewed(GitHubPullRequest pullRequest, String githubToken, String githubUsername) {
        String reviewsUrl = pullRequest.toApiUrl() + "/reviews";
        Request request = new Request.Builder()
            .url(reviewsUrl)
            .header(AUTHORIZATION, TOKEN + githubToken)
            .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Sentry.captureException(new IOException(HTTP_ERROR + response.code() + " for URL: " + reviewsUrl));
                return false;
            }
            String responseBody = response.body().string();
            JsonArray reviews = JsonParser.parseString(responseBody).getAsJsonArray();

            for (int i = 0; i < reviews.size(); i++) {
                JsonObject review = reviews.get(i).getAsJsonObject();
                if (review != null && review.has("user")) {
                    JsonObject user = review.getAsJsonObject("user");
                    if (user != null && user.has("login") && githubUsername.equalsIgnoreCase(user.get("login")
                        .getAsString())) {
                        if (review.has("state")) {
                            String state = review.get("state").getAsString();
                            if ("APPROVED".equalsIgnoreCase(state) || "CHANGES_REQUESTED".equalsIgnoreCase(state)
                                || "COMMENTED".equalsIgnoreCase(state)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            return false;
        }
    }
}
