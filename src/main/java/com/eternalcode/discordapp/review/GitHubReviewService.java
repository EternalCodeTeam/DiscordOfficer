package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GitHubReviewService {

    private final DiscordAppConfig discordAppConfig;
    private final JDA jda;
    private final Map<String, Long> githubDiscordMap;

    public GitHubReviewService(DiscordAppConfig discordAppConfig, JDA jda) {
        this.discordAppConfig = discordAppConfig;
        this.jda = jda;

        this.githubDiscordMap = this.discordAppConfig.reviewSystem.reviewers;
    }

    public boolean checkPullRequestTitle(String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    // create channel
    public long createForumPostWithPRTitleAndMention(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        MessageCreateData createData = MessageCreateData.fromContent(url);
        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData).complete().getThreadChannel().getIdLong();
    }

    public void mentionReviewers(String url, long messageId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.githubDiscordMap.get(reviewer);

            if (discordId != null) {
                User user = this.jda.getUserById(discordId);

                if (user != null) {
                    reviewersMention.append(user.getAsMention()).append(" ");
                }
            }
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, url);

        ThreadChannel threadChannel = this.jda.getThreadChannelById(messageId);
        threadChannel.sendMessage(message).queue();
    }


    public void automaticCreatePullRequests() throws IOException {
        List<String> pullRequests = GitHubReviewUtil.getPullRequests("EternalCodeTeam", List.of("vLuckyyy", "Hyd3r1"), List.of("DiscordOfficer", "EternalCore"), this.discordAppConfig.githubToken);

        for (String pullRequest : pullRequests) {

            String gitHubPullRequestApiUrl = GitHubReviewUtil.getGitHubPullRequestApiUrl(pullRequest);

            if (GitHubReviewUtil.isPullRequestTitleValid(gitHubPullRequestApiUrl)) {
                System.out.println(pullRequest);
            }

            Guild guild = this.jda.getGuildById(this.discordAppConfig.guildId);
            long forumPostWithPRTitleAndMention = this.createForumPostWithPRTitleAndMention(guild, pullRequest);
            this.mentionReviewers(pullRequest, forumPostWithPRTitleAndMention);
        }
    }

}
