package com.eternalcode.discordapp.feature.review;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepository;
import io.sentry.Sentry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import panda.std.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubReviewService.class);

    private static final String DM_REVIEW_MESSAGE = "You have been assigned as a reviewer for this pull request: %s";
    private static final String SERVER_REVIEW_MESSAGE =
        "%s, you have been assigned as a reviewer for this pull request: %s";

    private static final Duration GITHUB_API_RATE_LIMIT = Duration.ofSeconds(1);
    private final AppConfig appConfig;
    private final ConfigManager configManager;
    private final GitHubReviewMentionRepository mentionRepository;
    private volatile Instant lastGitHubApiCall = Instant.MIN;

    public GitHubReviewService(
        AppConfig appConfig,
        ConfigManager configManager,
        GitHubReviewMentionRepository mentionRepository
    ) {
        this.appConfig = appConfig;
        this.configManager = configManager;
        this.mentionRepository = mentionRepository;
    }

    public CompletableFuture<String> createReview(Guild guild, String url, JDA jda) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (guild == null) {
                    return "This command can only be used in a guild";
                }

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
                LOGGER.error("Error creating review", exception);
                throw new CompletionException(exception);
            }
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.error("Failed to create review", throwable);
            return "Something went wrong while creating review";
        });
    }

    public boolean checkPullRequestTitle(GitHubPullRequest url) throws IOException {
        this.waitForRateLimit();
        String pullRequestTitleFromUrl = GitHubReviewUtil.getPullRequestTitleFromUrl(url, this.appConfig.githubToken);

        return GitHubReviewUtil.isPullRequestTitleValid(pullRequestTitleFromUrl) &&
            GitHubReviewUtil.isTitleLengthValid(pullRequestTitleFromUrl);
    }

    public long createReviewForumPost(Guild guild, GitHubPullRequest pullRequest) throws IOException {
        this.waitForRateLimit();
        String pullRequestTitleFromUrl =
            GitHubReviewUtil.getPullRequestTitleFromUrl(pullRequest, this.appConfig.githubToken);
        ForumChannel forumChannel = guild.getForumChannelById(this.appConfig.reviewSystem.reviewForumId);

        if (forumChannel == null) {
            throw new IOException("Forum channel not found with ID: " + this.appConfig.reviewSystem.reviewForumId);
        }

        MessageCreateData createData = MessageCreateData.fromContent(pullRequestTitleFromUrl);

        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData)
            .setName(pullRequest.toUrl())
            .setTags(ForumTagSnowflake.fromId(this.appConfig.reviewSystem.inReviewForumTagId))
            .complete()
            .getThreadChannel()
            .getIdLong();
    }

    public CompletableFuture<Void> mentionReviewers(JDA jda, GitHubPullRequest pullRequest, long forumId) {
        return CompletableFuture.runAsync(this::waitForRateLimit).thenCompose(v -> CompletableFuture.supplyAsync(() -> {
            try {
                return GitHubReviewUtil.getReviewers(pullRequest, this.appConfig.githubToken);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to get reviewers for PR: {}", pullRequest.toUrl(), exception);
                return new ArrayList<String>();
            }
        })).thenCompose(assignedReviewers -> {
            if (assignedReviewers.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }

            List<CompletableFuture<Void>> mentionFutures = new ArrayList<>();
            StringBuilder reviewersMention = new StringBuilder();

            for (String reviewer : assignedReviewers) {
                GitHubReviewUser gitHubReviewUser = this.getReviewUserByUsername(reviewer);

                if (gitHubReviewUser == null || gitHubReviewUser.getDiscordId() == null) {
                    continue;
                }

                Long discordId = gitHubReviewUser.getDiscordId();

                CompletableFuture<Void> mentionFuture = this.mentionRepository.isMentioned(pullRequest, discordId)
                    .thenCompose(isMentioned -> {
                        if (isMentioned) {
                            return CompletableFuture.completedFuture(null);
                        }

                        return CompletableFuture.runAsync(() -> {
                            try {
                                User user = jda.getUserById(discordId);
                                if (user == null) {
                                    LOGGER.warn("User not found with ID: {}", discordId);
                                    return;
                                }

                                GitHubReviewNotificationType notificationType = gitHubReviewUser.getNotificationType();
                                String message = String.format(DM_REVIEW_MESSAGE, pullRequest.toUrl());

                                if (notificationType.isDmNotify()) {
                                    this.sendDirectMessage(user, message);
                                }

                                if (notificationType.isServerNotify()) {
                                    synchronized (reviewersMention) {
                                        reviewersMention.append(user.getAsMention()).append(" ");
                                    }
                                }

                                this.mentionRepository.markReviewerAsMentioned(pullRequest, discordId, forumId)
                                    .exceptionally(FutureHandler::handleException);
                            }
                            catch (Exception exception) {
                                Sentry.captureException(exception);
                                LOGGER.error("Error mentioning reviewer: {}", discordId, exception);
                            }
                        });
                    })
                    .exceptionally(FutureHandler::handleException);

                mentionFutures.add(mentionFuture);
            }

            return CompletableFuture.allOf(mentionFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    if (!reviewersMention.isEmpty()) {
                        String message = String.format(
                            SERVER_REVIEW_MESSAGE,
                            reviewersMention.toString().trim(),
                            pullRequest.toUrl());
                        ThreadChannel threadChannel = jda.getThreadChannelById(forumId);

                        if (threadChannel != null) {
                            threadChannel.sendMessage(message).queue(
                                success -> LOGGER.info("Server mention sent for PR: " + pullRequest.toUrl()),
                                failure -> {
                                    Sentry.captureException(failure);
                                    LOGGER.warn("Failed to send server mention", failure);
                                }
                            );
                        }
                        else {
                            LOGGER.warn("Thread channel not found with ID: {}", forumId);
                        }
                    }
                });
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.error("Error in mentionReviewers", throwable);
            return null;
        });
    }

    private void sendDirectMessage(User user, String message) {
        user.openPrivateChannel().queue(
            privateChannel -> privateChannel.sendMessage(message).queue(
                success -> LOGGER.info("DM sent to: " + user.getName()),
                failure -> {
                    if (failure instanceof RateLimitedException) {
                        LOGGER.warn("Rate limited when sending DM to: {}", user.getName());
                    }
                    else {
                        LOGGER.warn("Cannot send DM to: {} - {}", user.getName(), failure.getMessage());
                    }
                }
            ),
            throwable -> LOGGER.warn(
                "Cannot open private channel with: {} - {}", user.getName(), throwable.getMessage())
        );
    }

    public CompletableFuture<Void> mentionReviewersOnAllReviewChannels(JDA jda) {
        return CompletableFuture.runAsync(() -> {
            Guild guild = jda.getGuildById(this.appConfig.guildId);

            if (guild == null) {
                LOGGER.warn("Guild not found with ID: {}", this.appConfig.guildId);
                return;
            }

            List<CompletableFuture<Void>> channelFutures = new ArrayList<>();

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : this.getReviewThreads(forumChannel, false)) {
                    Result<GitHubPullRequest, IllegalArgumentException> result =
                        GitHubPullRequest.fromUrl(threadChannel.getName());

                    if (result.isErr()) {
                        continue;
                    }

                    CompletableFuture<Void> channelFuture =
                        this.mentionReviewers(jda, result.get(), threadChannel.getIdLong())
                            .exceptionally(FutureHandler::handleException);

                    channelFutures.add(channelFuture);
                }
            }

            CompletableFuture.allOf(channelFutures.toArray(new CompletableFuture[0]))
                .exceptionally(FutureHandler::handleException)
                .join();
        }).exceptionally(FutureHandler::handleException);
    }

    public boolean isReviewPostCreatedInGuild(Guild guild, String url) {
        if (guild == null) {
            LOGGER.warn("Guild is null when checking for existing review post");
            return false;
        }

        try {
            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    if (url.equals(threadChannel.getName())) {
                        return true;
                    }
                }
            }
        }
        catch (Exception exception) {
            LOGGER.warn("Error checking if review post exists", exception);
        }

        return false;
    }

    public CompletableFuture<Void> archiveMergedPullRequest(JDA jda) {
        return CompletableFuture.runAsync(() -> {
            Guild guild = jda.getGuildById(this.appConfig.guildId);

            if (guild == null) {
                LOGGER.warn("Guild not found with ID: {}", this.appConfig.guildId);
                return;
            }

            List<CompletableFuture<Void>> archiveFutures = new ArrayList<>();

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : this.getReviewThreads(forumChannel, true)) {
                    String name = threadChannel.getName();
                    Result<GitHubPullRequest, IllegalArgumentException> result = GitHubPullRequest.fromUrl(name);

                    if (result.isErr()) {
                        continue;
                    }

                    GitHubPullRequest pullRequest = result.get();

                    CompletableFuture<Void> archiveFuture = CompletableFuture.runAsync(() -> {
                        try {
                            this.waitForRateLimit();

                            boolean isMerged =
                                GitHubReviewUtil.isPullRequestMerged(pullRequest, this.appConfig.githubToken);
                            boolean isClosed =
                                GitHubReviewUtil.isPullRequestClosed(pullRequest, this.appConfig.githubToken);

                            if (isMerged || isClosed) {
                                this.cleanupClosedOrMergedPullRequest(threadChannel, pullRequest, isMerged);
                            }
                        }
                        catch (IOException exception) {
                            Sentry.captureException(exception);
                            LOGGER.warn("Error checking PR status for archival: {}", pullRequest.toUrl(), exception);
                        }
                    });

                    archiveFutures.add(archiveFuture);
                }
            }

            CompletableFuture.allOf(archiveFutures.toArray(new CompletableFuture[0]))
                .exceptionally(FutureHandler::handleException)
                .join();
        }).exceptionally(FutureHandler::handleException);
    }

    public boolean addUserToSystem(GitHubReviewUser gitHubReviewUser) {
        if (this.isUserExist(gitHubReviewUser)) {
            return false;
        }

        String githubUsername = gitHubReviewUser.getGithubUsername();
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            return false;
        }

        GitHubReviewUser normalizedUser = new GitHubReviewUser(
            gitHubReviewUser.getDiscordId(),
            githubUsername.trim(),
            gitHubReviewUser.getNotificationType()
        );

        this.appConfig.reviewSystem.reviewers.add(normalizedUser);
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

    public boolean updateUserNotificationType(Long discordId, GitHubReviewNotificationType newNotificationType) {
        for (GitHubReviewUser user : this.appConfig.reviewSystem.reviewers) {
            if (user.getDiscordId().equals(discordId)) {
                user.setNotificationType(newNotificationType);
                this.configManager.save(this.appConfig);
                return true;
            }
        }
        return false;
    }

    private boolean isUserExist(Long discordId) {
        return this.appConfig.reviewSystem.reviewers.stream()
            .anyMatch(user -> user.getDiscordId().equals(discordId));
    }

    private boolean isUserExist(GitHubReviewUser gitHubReviewUser) {
        if (gitHubReviewUser == null || gitHubReviewUser.getDiscordId() == null) {
            return false;
        }

        String githubUsername = gitHubReviewUser.getGithubUsername();
        if (githubUsername == null) {
            return this.isUserExist(gitHubReviewUser.getDiscordId());
        }

        return this.appConfig.reviewSystem.reviewers.stream()
            .anyMatch(user ->
                user.getDiscordId().equals(gitHubReviewUser.getDiscordId()) ||
                    (user.getGithubUsername() != null &&
                        user.getGithubUsername().equalsIgnoreCase(githubUsername.trim())));
    }

    public List<GitHubReviewUser> getListOfUsers() {
        return new ArrayList<>(this.appConfig.reviewSystem.reviewers);
    }

    public GitHubReviewUser getReviewUserByUsername(String githubUsername) {
        if (githubUsername == null || githubUsername.trim().isEmpty()) {
            return null;
        }

        return this.appConfig.reviewSystem.reviewers.stream()
            .filter(user -> user.getGithubUsername() != null &&
                user.getGithubUsername().equalsIgnoreCase(githubUsername.trim()))
            .findFirst()
            .orElse(null);
    }

    private synchronized void waitForRateLimit() {
        Instant now = Instant.now();
        Instant nextAllowed = this.lastGitHubApiCall.plus(GITHUB_API_RATE_LIMIT);

        if (now.isBefore(nextAllowed)) {
            try {
                long sleepMs = Duration.between(now, nextAllowed).toMillis();
                Thread.sleep(sleepMs);
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Interrupted while waiting for rate limit");
            }
        }

        this.lastGitHubApiCall = Instant.now();
    }

    private void cleanupClosedOrMergedPullRequest(
        ThreadChannel threadChannel,
        GitHubPullRequest pullRequest,
        boolean isMerged
    ) {
        GitHubReviewStatus reviewStatus = isMerged ? GitHubReviewStatus.MERGED : GitHubReviewStatus.CLOSED;
        this.mentionRepository.updateReviewStatus(pullRequest, reviewStatus);

        String state = isMerged ? "merged" : "closed";
        threadChannel.delete()
            .queue(
                success -> LOGGER.info("Deleted {} PR thread: {}", state, pullRequest.toUrl()),
                failure -> LOGGER.warn("Failed to delete {} PR thread: {}", state, pullRequest.toUrl(), failure)
            );
    }

    private List<ThreadChannel> getReviewThreads(ForumChannel forumChannel, boolean includeArchived) {
        Map<Long, ThreadChannel> threadsById = new LinkedHashMap<>();

        for (ThreadChannel activeThread : forumChannel.getThreadChannels()) {
            threadsById.put(activeThread.getIdLong(), activeThread);
        }

        if (!includeArchived) {
            return new ArrayList<>(threadsById.values());
        }

        try {
            for (ThreadChannel archivedThread : forumChannel.retrieveArchivedPublicThreadChannels().complete()) {
                threadsById.put(archivedThread.getIdLong(), archivedThread);
            }
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to retrieve archived threads for forum: {}", forumChannel.getId(), exception);
        }

        return new ArrayList<>(threadsById.values());
    }
}

