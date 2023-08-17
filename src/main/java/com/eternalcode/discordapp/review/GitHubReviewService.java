package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import panda.std.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHubReviewService {

    private final AppConfig discordAppConfig;
    private final ConfigManager configManager;
    private final GitHubReviewMentionRepository mentionRepository = new GitHubReviewMentionRepositoryImpl();

    public GitHubReviewService(AppConfig discordAppConfig, ConfigManager configManager) {
        this.discordAppConfig = discordAppConfig;
        this.configManager = configManager;
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
        } catch (IOException exception) {
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

        MessageCreateData createData = MessageCreateData.fromContent(GitHubReviewUtil.getPullRequestTitleFromUrl(pullRequest, this.discordAppConfig.githubToken));

        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData)
            .setName(pullRequest.toUrl())
            .setTags(ForumTagSnowflake.fromId(this.discordAppConfig.reviewSystem.inReviewForumTagId))
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
            GitHubReviewUser gitHubReviewUser = this.getReviewUserByUsername(reviewer);
            if (gitHubReviewUser == null) {
                continue;
            }

            Long discordId = gitHubReviewUser.discordId();
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
                } catch (Exception ignored) {

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

        if (guild == null) {
            return;
        }

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

    public void archiveMergedPullRequest(JDA jda) {
        try {
            Guild guild = jda.getGuildById(this.discordAppConfig.guildId);

            if (guild == null) {
                return;
            }

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    String name = threadChannel.getName();
                    Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(name);

                    if (result.isErr()) {
                        continue;
                    }

                    GitHubPullRequest pullRequest = result.get();
                    if (GitHubReviewUtil.isPullRequestMerged(pullRequest, this.discordAppConfig.githubToken)) {
                        threadChannel.getManager().setLocked(true).setArchived(true).setAppliedTags(ForumTagSnowflake.fromId(this.discordAppConfig.reviewSystem.mergedTagId)).queue();
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean addUserToSystem(GitHubReviewUser gitHubReviewUser) {
        if (this.isUserExist(gitHubReviewUser)) {
            return false;
        }

        this.discordAppConfig.reviewSystem.reviewers.add(gitHubReviewUser);
        this.configManager.save(this.discordAppConfig);

        return true;
    }

    public boolean removeUserFromSystem(Long discordId) {
        if (!this.isUserExist(discordId)) {
            return false;
        }

        this.discordAppConfig.reviewSystem.reviewers.removeIf(user -> user.discordId().equals(discordId));
        this.configManager.save(this.discordAppConfig);

        return true;
    }

    private boolean isUserExist(Long discordId) {
        return this.discordAppConfig.reviewSystem.reviewers.stream()
            .anyMatch(user -> user.discordId().equals(discordId));
    }

    private boolean isUserExist(GitHubReviewUser gitHubReviewUser) {
        return this.discordAppConfig.reviewSystem.reviewers.stream()
            .anyMatch(user -> user.discordId().equals(gitHubReviewUser.discordId()));
    }

    public List<GitHubReviewUser> getListOfUsers() {
        return new ArrayList<>(this.discordAppConfig.reviewSystem.reviewers);
    }

    public GitHubReviewUser getReviewUserByUsername(String githubUsername) {
        return this.discordAppConfig.reviewSystem.reviewers.stream()
            .filter(user -> user.githubUsername().equals(githubUsername))
            .findFirst()
            .orElse(null);
    }
}
