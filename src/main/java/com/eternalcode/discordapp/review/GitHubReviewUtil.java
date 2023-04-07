package com.eternalcode.discordapp.review;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GitHubReviewUtil {

    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final Gson GSON = new Gson();

    private GitHubReviewUtil() {}

    public static boolean isPullRequestTitleValid(String title) {
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

    public static List<String> getReviewers(GitHubPullRequest pullRequest, String githubToken) {
        Request request = new Request.Builder()
                .url(pullRequest.toApiUrl())
                .header("Authorization", "token" + githubToken)
                .build();

        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            String responseBody = response.body().string();

            JsonObject json = GSON.fromJson(responseBody, JsonObject.class);
            JsonArray requestedReviewers = json.getAsJsonArray("requested_reviewers");

            List<String> reviewers = new ArrayList<>();
            for (int i = 0; i < requestedReviewers.size(); i++) {
                JsonObject reviewer = requestedReviewers.get(i).getAsJsonObject();
                String reviewerLogin = reviewer.get("login").getAsString();
                reviewers.add(reviewerLogin);
            }

            return reviewers;
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static String getPullRequestTitleFromUrl(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(pullRequest.toApiUrl())
                .header("Authorization", "token" + githubToken)
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        String string = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();

        return jsonObject.get("title").getAsString();
    }

    public static boolean isPullRequestMerged(GitHubPullRequest pullRequest, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(pullRequest.toApiUrl())
                .header("Authorization", "token " + githubToken)
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        JsonObject json = GSON.fromJson(response.body().string(), JsonObject.class);
        return json.get("merged").getAsBoolean();
    }

}
