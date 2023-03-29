package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GitHubReviewService {

    private final OkHttpClient httpClient;

    private final DiscordAppConfig discordAppConfig;
    private final Map<String, Long> githubDiscordMap;

    public GitHubReviewService(OkHttpClient httpClient, DiscordAppConfig discordAppConfig) {
        this.httpClient = httpClient;
        this.discordAppConfig = discordAppConfig;

        this.githubDiscordMap = this.discordAppConfig.reviewSystem.reviewers;
    }

    boolean checkPullRequestTitle(String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.httpClient, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    // create channel
    long createForumPostWithPRTitleAndMention(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, httpClient, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        MessageCreateData createData = MessageCreateData.fromContent(url);
        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData).complete().getThreadChannel().getIdLong();
    }

    boolean mentionReviewers(SlashCommandEvent event, String url, long messageId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.httpClient, this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            event.reply("No reviewers assigned to this pull request").setEphemeral(true).queue();
            return false;
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

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, url);

        ThreadChannel threadChannel = event.getJDA().getThreadChannelById(messageId);
        threadChannel.sendMessage(message).queue();
        return true;
    }

}
