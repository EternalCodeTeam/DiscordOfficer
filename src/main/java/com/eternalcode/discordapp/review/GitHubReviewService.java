package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubReviewService {

    private final AppConfig discordAppConfig;

    public GitHubReviewService(AppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;
    }

    public String createReview(Guild guild, String url, JDA jda) {
        try {
            if (this.isReviewPostCreatedInGuild(guild, url)) {
                return "Review already exists";
            }

            if (!GitHubReviewUtil.isPullRequestUrl(url)) {
                return "URL is not a valid, please provide a valid GitHub pull request URL";
            }

            if (!this.checkPullRequestTitle(url)) {
                return "Pull request title is not valid, please use GH-<number> as title";
            }

            long messageId = this.createReviewForumPost(guild, url);
            this.mentionReviewers(jda, url, messageId);

            return "Review created";
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return "Something went wrong";
        }
    }

    public boolean checkPullRequestTitle(String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    public long createReviewForumPost(Guild guild, String url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(1090383282744590396L);

        MessageCreateData createData = MessageCreateData.fromContent(url);
        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData).setName(url).complete().getThreadChannel().getIdLong();
    }

    public void mentionReviewers(JDA jda, String url, long forumId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(GitHubReviewUtil.getGitHubPullRequestApiUrl(url), this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.discordAppConfig.reviewSystem.reviewers.get(reviewer);
            if (discordId == null) {
                continue;
            }

            User user = jda.getUserById(discordId);
            if (user == null) {
                continue;
            }

            user.openPrivateChannel().queue(privateChannel -> {
                try {
                    privateChannel.sendMessage(String.format("You have been assigned as a reviewer for this pull request: %s", url)).queue();
                }
                catch (Exception ignored) {

                }
            });

            reviewersMention.append(user.getAsMention()).append(" ");
        }

        if (reviewersMention.length() == 0) {
            return;
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, url);

        ThreadChannel threadChannel = jda.getThreadChannelById(forumId);
        threadChannel.sendMessage(message).queue();
    }

    public void mentionReviewersOnAllReviewChannels(JDA jda) {
        Guild guild = jda.getGuildById(this.discordAppConfig.guildId);

        for (ForumChannel forumChannel : guild.getForumChannels()) {
            for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                this.mentionReviewers(jda, threadChannel.getName(), threadChannel.getIdLong());
            }
        }
    }

    public boolean isReviewPostCreatedInGuild(Guild guild, String url) {
        List<ThreadChannel> threadChannels = new ArrayList<>();

        for (ForumChannel forumChannel : guild.getForumChannels()) {
            threadChannels.addAll(forumChannel.getThreadChannels());
        }

        for (ThreadChannel threadChannel : threadChannels) {
            if (threadChannel.getName().equals(url)) {
                return true;
            }
        }

        return false;
    }


    public void deleteMergedPullRequests(JDA jda) {
        try {
            Guild guild = jda.getGuildById(this.discordAppConfig.guildId);

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    if (GitHubReviewUtil.isPullRequestMerged(threadChannel.getName(), this.discordAppConfig.githubToken)) {
                        threadChannel.delete().queue();
                    }
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
