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

public final class GitHubReviewUtil {

    private static final String GITHUB_PULL_REQUEST_REGEX = "^https:\\/\\/github\\.com\\/([a-zA-Z0-9-_]+\\/[a-zA-Z0-9-_]+)\\/pull\\/([0-9]+)$";
    private static final String GITHUB_PULL_REQUEST_TITLE_CONVENTION = "^(GH)-\\d+ .+$";

    public static boolean isPullRequestUrl(String url) {
        return url.matches(GITHUB_PULL_REQUEST_REGEX);
    }

    public static boolean isPullRequestTitleValid(String title) {
        return title.matches(GITHUB_PULL_REQUEST_TITLE_CONVENTION);
    }

    public static List<String> getReviewers(String url, OkHttpClient client, String githubToken) {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token" + githubToken)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
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

    public static String getPullRequestTitleFromUrl(String url, OkHttpClient client, String githubToken) throws IOException {
        Request request = new Request.Builder()
                .url(GitHubReviewUtil.getGitHubPullRequestApiUrl(url))
                .header("Authorization", "token" + githubToken)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        String string = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();

        return jsonObject.get("title").getAsString();
    }

}
