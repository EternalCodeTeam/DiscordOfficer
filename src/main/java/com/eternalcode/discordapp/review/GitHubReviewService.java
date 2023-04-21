package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import panda.std.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubReviewService {

    private final AppConfig discordAppConfig;
    private final GitHubReviewMentionRepository mentionRepository = new GitHubReviewMentionRepositoryImpl();

    public GitHubReviewService(AppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;
    }

    public String createReview(Guild guild, String url, JDA jda) {
        try {
            if (this.isReviewPostCreatedInGuild(guild, url)) {
                return "Review already exists";
            }

            Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(url);
            if (result.isErr()) {
                return "URL is not a valid, please provide a valid GitHub pull request URL";
            }

            GitHubPullRequest pullRequest = result.get();
            if (!this.checkPullRequestTitle(pullRequest)) {
                return "Pull request title is not valid, please use GH-<number> as title";
            }

            long messageId = this.createReviewForumPost(guild, pullRequest);
            this.mentionReviewers(jda, pullRequest, messageId);

            return "Review created";
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return "Something went wrong";
        }
    }

    public boolean checkPullRequestTitle(GitHubPullRequest url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.discordAppConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl);
    }

    public long createReviewForumPost(Guild guild, GitHubPullRequest pullRequest) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(pullRequest, this.discordAppConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(this.discordAppConfig.reviewSystem.reviewForumId);

        MessageCreateData createData = MessageCreateData.fromContent(pullRequest.toUrl());

        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData)
                .setName(pullRequest.toUrl())
                .complete()
                .getThreadChannel()
                .getIdLong();
    }

    public void mentionReviewers(JDA jda, GitHubPullRequest pullRequest, long forumId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(pullRequest, this.discordAppConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();
        for (String reviewer : assignedReviewers) {
            Long discordId = this.discordAppConfig.reviewSystem.reviewers.get(reviewer);
            if (discordId == null) {
                continue;
            }

            if (this.mentionRepository.isMentioned(pullRequest, discordId)) {
                continue;
            }

            User user = jda.getUserById(discordId);
            if (user == null) {
                continue;
            }

            user.openPrivateChannel().queue(privateChannel -> {
                try {
                    privateChannel.sendMessage(String.format("You have been assigned as a reviewer for this pull request: %s", pullRequest.toUrl())).queue();
                }
                catch (Exception ignored) {

                }
            });

            reviewersMention.append(user.getAsMention()).append(" ");
            this.mentionRepository.markReviewerAsMentioned(pullRequest, discordId);
        }

        if (reviewersMention.length() == 0) {
            return;
        }

        String message = String.format("%s, you have been assigned as a reviewer for this pull request: %s", reviewersMention, pullRequest.toUrl());

        ThreadChannel threadChannel = jda.getThreadChannelById(forumId);

        if (threadChannel == null) {
            return;
        }

        threadChannel.sendMessage(message).queue();
    }

    public void mentionReviewersOnAllReviewChannels(JDA jda) {
        Guild guild = jda.getGuildById(this.discordAppConfig.guildId);

        for (ForumChannel forumChannel : guild.getForumChannels()) {
            for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(threadChannel.getName());

                if (result.isErr()) {
                    continue;
                }

                this.mentionReviewers(jda, result.get(), threadChannel.getIdLong());
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
                    String name = threadChannel.getName();
                    Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(name);

                    if (result.isErr()) {
                        continue;
                    }

                    GitHubPullRequest pullRequest = result.get();
                    if (GitHubReviewUtil.isPullRequestMerged(pullRequest, this.discordAppConfig.githubToken)) {
                        threadChannel.getManager().setLocked(true).setArchived(true).queue();
                    }
                }
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
