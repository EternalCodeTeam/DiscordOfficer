package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.review.pr.PullRequestApiExtractor;
import com.eternalcode.discordapp.review.pr.PullRequestInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GitHubReviewUtil {

    private static final String GITHUB_PULL_REQUEST_REGEX = "^https://github\\.com/([a-zA-Z0-9-_]+/[a-zA-Z0-9-_]+)/pull/([0-9]+)$";
    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final Gson GSON = new Gson();

    public static boolean isPullRequestUrl(String url) {
        return url.matches(GITHUB_PULL_REQUEST_REGEX);
    }

    public static boolean isPullRequestTitleValid(String title) {
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

    public static List<String> getReviewers(String url, String githubToken) {
        Request request = new Request.Builder()
                .url(url)
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
            return null;
        }
    }

    public static String getGitHubPullRequestApiUrl(String url) {
        PullRequestInfo pullRequestInfo = PullRequestApiExtractor.extractPRInfoFromLink(url);

        return String.format("https://api.github.com/repos/%s/%s/pulls/%s", pullRequestInfo.getOwner(), pullRequestInfo.getRepo(), pullRequestInfo.getNumber());
    }

    public static String getPullRequestTitleFromUrl(String url, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(GitHubReviewUtil.getGitHubPullRequestApiUrl(url))
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

    public static String getPullRequestAuthorUsernameFromUrl(String url, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(GitHubReviewUtil.getGitHubPullRequestApiUrl(url))
                .header("Authorization", "token" + githubToken)
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        String responseBody = response.body().string();
        JsonObject jsonObject = GSON.fromJson(responseBody, JsonObject.class);
        JsonObject userObject = jsonObject.getAsJsonObject("user");
        return userObject.get("login").getAsString();
    }

    public static boolean isPullRequestMerged(String url, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(GitHubReviewUtil.getGitHubPullRequestApiUrl(url))
                .header("Authorization", "token " + githubToken)
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        JsonObject json = GSON.fromJson(response.body().string(), JsonObject.class);
        return json.get("merged").getAsBoolean();
    }

    public static String getDiscordIdFromGitHubUsername(String githubUsername, Map<String, Long> githubToDiscordMap) {
        return githubToDiscordMap.get(githubUsername).toString();
    }

}
