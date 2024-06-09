package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import panda.std.Result;

public class GitHubReviewService {

    private final static Logger LOGGER = Logger.getLogger(GitHubReviewService.class.getName());

    private static final String DM_REVIEW_MESSAGE = "You have been assigned as a reviewer for this pull request: %s";
    private static final String SERVER_REVIEW_MESSAGE =
        "%s, you have been assigned as a reviewer for this pull request: %s";

    private final AppConfig appConfig;
    private final ConfigManager configManager;
    private final GitHubReviewMentionRepository mentionRepository = new GitHubReviewMentionRepositoryImpl();

    public GitHubReviewService(AppConfig appConfig, ConfigManager configManager) {
        this.appConfig = appConfig;
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
                return "Pull request title is not valid, please use GH-<number> as title and keep it under 100 characters";
            }

            long messageId = this.createReviewForumPost(guild, pullRequest);
            this.mentionReviewers(jda, pullRequest, messageId);

            return "Review created";
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            exception.printStackTrace();
            return "Something went wrong";
        }
    }

    public boolean checkPullRequestTitle(GitHubPullRequest url) throws IOException {
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.appConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl) &&
            GitHubReviewUtil.isTitleLengthValid(pullRequestTitleFromUrl);
    }

    public long createReviewForumPost(Guild guild, GitHubPullRequest pullRequest) throws IOException {
        String pullRequestTitleFromUrl =
            GitHubReviewUtil.getPullRequestTitleFromUrl(pullRequest, this.appConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(this.appConfig.reviewSystem.reviewForumId);

        MessageCreateData createData = MessageCreateData.fromContent(GitHubReviewUtil.getPullRequestTitleFromUrl(
            pullRequest,
            this.appConfig.githubToken
        ));

        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData)
            .setName(pullRequest.toUrl())
            .setTags(ForumTagSnowflake.fromId(this.appConfig.reviewSystem.inReviewForumTagId))
            .complete()
            .getThreadChannel()
            .getIdLong();
    }

    public void mentionReviewers(JDA jda, GitHubPullRequest pullRequest, long forumId) {
        List<String> assignedReviewers = GitHubReviewUtil.getReviewers(pullRequest, this.appConfig.githubToken);

        if (assignedReviewers.isEmpty()) {
            return;
        }

        StringBuilder reviewersMention = new StringBuilder();

        for (String reviewer : assignedReviewers) {
            GitHubReviewUser gitHubReviewUser = this.getReviewUserByUsername(reviewer);

            if (gitHubReviewUser == null) {
                continue;
            }

            Long discordId = gitHubReviewUser.getDiscordId();

            if (discordId != null && !this.mentionRepository.isMentioned(pullRequest, discordId)) {
                User user = jda.getUserById(discordId);
                GitHubReviewNotificationType notificationType = gitHubReviewUser.getNotificationType();

                if (user == null) {
                    return;
                }

                String message = String.format(DM_REVIEW_MESSAGE, pullRequest.toUrl());

                if (notificationType.isDmNotify()) {
                    try {
                        user.openPrivateChannel().queue(
                            privateChannel -> privateChannel.sendMessage(message).queue(),
                            throwable -> LOGGER.warning("Cannot send message to: " + user.getName()));
                    }
                    catch (Exception exception) {
                        LOGGER.warning("Cannot send message to: " + user.getName());
                    }
                }
                if (notificationType.isServerNotify()) {
                    reviewersMention.append(user.getAsMention()).append(" ");
                }

                this.mentionRepository.markReviewerAsMentioned(pullRequest, discordId);
            }
        }

        if (!reviewersMention.isEmpty()) {
            String message = String.format(SERVER_REVIEW_MESSAGE, reviewersMention, pullRequest.toUrl());
            ThreadChannel threadChannel = jda.getThreadChannelById(forumId);

            if (threadChannel != null) {
                threadChannel.sendMessage(message).queue();
            }
        }
    }

    public void mentionReviewersOnAllReviewChannels(JDA jda) {
        Guild guild = jda.getGuildById(this.appConfig.guildId);

        if (guild == null) {
            return;
        }

        for (ForumChannel forumChannel : guild.getForumChannels()) {
            for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                Result<GitHubPullRequest, IllegalArgumentException> result =
                    GitHubPullRequest.fromUrl(threadChannel.getName());

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
            Guild guild = jda.getGuildById(this.appConfig.guildId);

            if (guild == null) {
                return;
            }

            AppConfig.ReviewSystem reviewSystem = this.appConfig.reviewSystem;

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    String name = threadChannel.getName();
                    Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(name);

                    if (result.isErr()) {
                        continue;
                    }

                    GitHubPullRequest pullRequest = result.get();

                    if (GitHubReviewUtil.isPullRequestMerged(pullRequest, this.appConfig.githubToken)) {
                        threadChannel.getManager()
                            .setLocked(true)
                            .setArchived(true)
                            .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.mergedTagId))
                            .queue();
                    }

                    if (GitHubReviewUtil.isPullRequestClosed(pullRequest, this.appConfig.githubToken)) {
                        threadChannel.getManager()
                            .setLocked(true)
                            .setArchived(true)
                            .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.closedTagId))
                            .queue();
                    }
                }
            }
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            exception.printStackTrace();
        }
    }

    public boolean addUserToSystem(GitHubReviewUser gitHubReviewUser) {
        if (this.isUserExist(gitHubReviewUser)) {
            return false;
        }

        this.appConfig.reviewSystem.reviewers.add(gitHubReviewUser);
        this.configManager.save(this.appConfig);

        return true;
    }

    public boolean removeUserFromSystem(Long discordId) {
        if (!this.isUserExist(discordId)) {
            return false;
        }

        this.appConfig.reviewSystem.reviewers.removeIf(user -> user.getDiscordId().equals(discordId));
        this.configManager.save(this.appConfig);

        return true;
    }

    public void updateUserNotificationType(Long discordId, GitHubReviewNotificationType newNotificationType) {
        for (GitHubReviewUser user : this.appConfig.reviewSystem.reviewers) {

            if (user.getDiscordId().equals(discordId)) {
                user.setNotificationType(newNotificationType);
                this.configManager.save(this.appConfig);

                return;
            }
        }
    }

    private boolean isUserExist(Long discordId) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .anyMatch(user -> user.getDiscordId().equals(discordId));
    }

    private boolean isUserExist(GitHubReviewUser gitHubReviewUser) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .anyMatch(user -> user.getDiscordId().equals(gitHubReviewUser.getDiscordId()));
    }

    public List<GitHubReviewUser> getListOfUsers() {
        return new ArrayList<>(this.appConfig.reviewSystem.reviewers);
    }

    public GitHubReviewUser getReviewUserByUsername(String githubUsername) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .filter(user -> user.getGithubUsername().equals(githubUsername))
            .findFirst()
            .orElse(null);
    }
}
