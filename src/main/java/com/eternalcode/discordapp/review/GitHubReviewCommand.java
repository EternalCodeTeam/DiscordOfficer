package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GitHubReviewCommand extends SlashCommand {

    private final OkHttpClient httpClient;
    private final DiscordAppConfig discordAppConfig;
    private final Map<String, Long> githubDiscordMap;

    public GitHubReviewCommand(OkHttpClient httpClient, DiscordAppConfig discordAppConfig) {
        this.name = "review";
        this.help = "Review a GitHub pull request";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.STRING, "url", "Review a GitHub pull request")
                        .setRequired(true)
        );

        this.httpClient = httpClient;
        this.discordAppConfig = discordAppConfig;
        this.githubDiscordMap = discordAppConfig.reviewSystem.reviewers;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String url = event.getOption("url").getAsString();

        boolean pullRequestUrl = GitHubReviewUtil.isPullRequestUrl(url);

        if (!pullRequestUrl) {
            event.reply("Invalid GitHub pull request URL").setEphemeral(true).queue();
            return;
        }

        try {
            boolean checkPullRequestTitle = this.checkPullRequestTitle(url);

            if (!checkPullRequestTitle) {
                event.reply("Invalid pull request title, use GH-NUMBER convention").setEphemeral(true).queue();
                return;
            }
        }
        catch (IOException exception) {
            event.reply("Failed to check pull request title").setEphemeral(true).queue();
            return;
        }

        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.httpClient);

        if (assignedReviewers.isEmpty()) {
            event.reply("No reviewers assigned to this pull request").setEphemeral(true).queue();
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.githubDiscordMap.get(reviewer);

            if (discordId != null) {
                User user = event.getJDA().getUserById(discordId);

                if (user != null) {
                    reviewersMention.append(user.getAsMention()).append(" ");
                }
            }
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention.toString(), url);
        event.reply(message).queue();
    }

    boolean checkPullRequestTitle(String url) throws IOException {
        Request request = new Request.Builder()
                .url(GitHubReviewUtil.getGitHubPullRequestApiUrl(url))
                .build();

        Response response = this.httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("HTTP Error: " + response.code());
        }

        String string = response.body().string();
        JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
        String title = jsonObject.get("title").getAsString();

        return GitHubReviewUtil.isPullRequestTitleValid(title);
    }


}
