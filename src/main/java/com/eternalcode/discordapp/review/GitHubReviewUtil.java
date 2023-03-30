package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.review.pr.PullRequestApiExtractor;
import com.eternalcode.discordapp.review.pr.PullRequestInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
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

    public static String getDiscordIdFromGitHubUsername(String githubUsername, Map<String, Long> githubToDiscordMap) {
        return githubToDiscordMap.get(githubUsername).toString();
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


    // TODO: This method is a mess, refactor it
    public static List<String> getPullRequests(String organizationName, List<String> userList, List<String> repoList, String githubToken) throws IOException {
        List<String> pullRequestsLinks = new ArrayList<>();

        for (String repoName : repoList) {
            String repoPullsUrl = String.format("https://api.github.com/repos/%s/%s/pulls", organizationName, repoName);

            Request repoPullsRequest = new Request.Builder()
                    .url(repoPullsUrl)
                    .header("Authorization", "token" + githubToken)
                    .build();

            System.out.println("Requesting: " + repoPullsUrl);

            try (Response repoPullsResponse = HTTP_CLIENT.newCall(repoPullsRequest).execute()) {
                JsonElement responseJson = JsonParser.parseString(repoPullsResponse.body().string());

                if (responseJson.isJsonArray()) {
                    JsonArray pullRequests = responseJson.getAsJsonArray();

                    for (JsonElement pullRequestElement : pullRequests) {
                        String username = pullRequestElement.getAsJsonObject().get("user").getAsJsonObject().get("login").getAsString();

                        if (userList.contains(username)) {
                            String prLink = pullRequestElement.getAsJsonObject().get("html_url").getAsString();
                            pullRequestsLinks.add(prLink);
                        }
                    }
                } else if (responseJson.isJsonObject()) {
                    JsonObject errorObject = responseJson.getAsJsonObject();
                    throw new IOException("API returned an error: " + errorObject.toString());
                } else {
                    throw new IOException("API returned an unexpected response format: " + responseJson.toString());
                }
            }

        }

        return pullRequestsLinks;
    }



    /*
                            try {
                            String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(message.getContentRaw(), this.discordAppConfig.githubToken);

                            boolean pullRequestTitleValid = GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);

                            if (!pullRequestTitleValid) {
                                String pullRequestAuthorUsernameFromUrl = GitHubReviewUtil.getPullRequestAuthorUsernameFromUrl(message.getContentRaw(), this.discordAppConfig.githubToken);
                                String discordIdFromGitHubUsername = GitHubReviewUtil.getDiscordIdFromGitHubUsername(pullRequestAuthorUsernameFromUrl, this.discordAppConfig.reviewSystem.reviewers);

                                threadChannel.sendMessage(String.format("<@%s> Pull request title is invalid, please fix it!", discordIdFromGitHubUsername)).queue();
                            }
                        }
                        catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
     */

}
