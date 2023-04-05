package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.util.List;

public class GitHubReviewService {

    private final AppConfig discordAppConfig;
    private final ReviewRepository reviewRepository;

    public GitHubReviewService(AppConfig discordAppConfig, ReviewRepository reviewRepository) {
        this.discordAppConfig = discordAppConfig;
        this.reviewRepository = reviewRepository;
    }

    public String createReview(Guild guild, String url, JDA jda) {

        try {
            if (!GitHubReviewUtil.isPullRequestUrl(url)) {
                return "URL is not a valid GitHub pull request";
            }

            if (!checkPullRequestTitle(url)) {
                return "Pull request title is not valid";
            }

            long messageId = createReviewForumPost(guild, url);
            this.mentionReviewers(jda, url, messageId);

            Review review = new Review(messageId, url, GitHubReviewUtil.getPullRequestAuthorUsernameFromUrl(url, this.discordAppConfig.githubToken), GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken));
            this.reviewRepository.saveReview(review);

            return "Review created";
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return "Something went wrong";
    }

    public boolean checkPullRequestTitle(String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    public long createReviewForumPost(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        MessageCreateData createData = MessageCreateData.fromContent(url);
        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData).complete().getThreadChannel().getIdLong();
    }

    public void mentionReviewers(JDA jda, String url, long messageId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.discordAppConfig.reviewSystem.reviewers.get(reviewer);

            if (discordId != null) {
                User user = jda.getUserById(discordId);

                if (user != null) {
                    reviewersMention.append(user.getAsMention()).append(" ");
                }
            }
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, url);

        ThreadChannel threadChannel = jda.getThreadChannelById(messageId);
        threadChannel.sendMessage(message).queue();
    }

}
