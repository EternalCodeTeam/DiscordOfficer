package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.review.pr.PullRequestExtractor;
import com.eternalcode.discordapp.review.pr.PullRequestInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class GitHubReviewCommand extends SlashCommand {

    private final OkHttpClient httpClient;

    public GitHubReviewCommand(OkHttpClient httpClient) {
        this.name = "review";
        this.help = "Review a GitHub pull request";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.STRING, "url", "Review a GitHub pull request")
                        .setRequired(true)
        );

        this.httpClient = httpClient;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String url = event.getOption("url").getAsString();

        boolean pullRequestUrl = GitHubReviewUtil.isPullRequestUrl(url);

        if (!pullRequestUrl) {
            event.reply("Invalid GitHub pull request URL").setEphemeral(true).queue();
        }

        try {
            boolean checkPullRequestTitle = this.checkPullRequestTitle(url);

            if (checkPullRequestTitle) {
                event.reply("Hurray! The pull request title is valid").setEphemeral(true).queue();
            }
            else {
                event.reply("The pull request title is invalid").setEphemeral(true).queue();
            }


        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

    boolean checkPullRequestTitle(String url) throws IOException {
        PullRequestInfo pullRequestInfo = PullRequestExtractor.extractPRInfoFromLink(url);

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%s", pullRequestInfo.getOwner(), pullRequestInfo.getRepo(), pullRequestInfo.getNumber());

        Request request = new Request.Builder()
                .url(apiUrl)
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
