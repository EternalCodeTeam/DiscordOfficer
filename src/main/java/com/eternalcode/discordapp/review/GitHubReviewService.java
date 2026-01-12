package com.eternalcode.discordapp.review;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepository;
import io.sentry.Sentry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import panda.std.Result;

public class GitHubReviewService {

    private final static Logger LOGGER = Logger.getLogger(GitHubReviewService.class.getName());

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
                LOGGER.log(Level.SEVERE, "Error creating review", exception);
                throw new CompletionException(exception);
            }
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.log(Level.SEVERE, "Failed to create review", throwable);
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

        MessageCreateData createData = MessageCreateData.fromContent(pullRequest.toUrl());

        return forumChannel.createForumPost(pullRequestTitleFromUrl, createData)
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
                LOGGER.log(Level.WARNING, "Failed to get reviewers for PR: " + pullRequest.toUrl(), exception);
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
                                    LOGGER.warning("User not found with ID: " + discordId);
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
                                LOGGER.log(Level.SEVERE, "Error mentioning reviewer: " + discordId, exception);
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
                                    LOGGER.log(Level.WARNING, "Failed to send server mention", failure);
                                }
                            );
                        }
                        else {
                            LOGGER.warning("Thread channel not found with ID: " + forumId);
                        }
                    }
                });
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.log(Level.SEVERE, "Error in mentionReviewers", throwable);
            return null;
        });
    }

    private void sendDirectMessage(User user, String message) {
        user.openPrivateChannel().queue(
            privateChannel -> privateChannel.sendMessage(message).queue(
                success -> LOGGER.info("DM sent to: " + user.getName()),
                failure -> {
                    if (failure instanceof RateLimitedException) {
                        LOGGER.warning("Rate limited when sending DM to: " + user.getName());
                    }
                    else {
                        LOGGER.warning("Cannot send DM to: " + user.getName() + " - " + failure.getMessage());
                    }
                }
            ),
            throwable -> LOGGER.warning(
                "Cannot open private channel with: " + user.getName() + " - " + throwable.getMessage())
        );
    }

    private Result<GitHubPullRequest, IllegalArgumentException> getPullRequestFromThread(ThreadChannel threadChannel) {
        try {
            // Try to get the first message in the thread which should contain the PR URL
            List<net.dv8tion.jda.api.entities.Message> messages = threadChannel.getHistory()
                .retrievePast(1)
                .complete();
            
            if (messages.isEmpty()) {
                LOGGER.log(Level.WARNING, "No messages found in thread: " + threadChannel.getId());
                return Result.error(new IllegalArgumentException("No messages in thread"));
            }
            
            String firstMessageContent = messages.get(0).getContentRaw();
            return GitHubPullRequest.fromUrl(firstMessageContent);
        }
        catch (net.dv8tion.jda.api.exceptions.ErrorResponseException exception) {
            LOGGER.log(Level.WARNING, "Discord API error retrieving message from thread: " + threadChannel.getId(), exception);
            return Result.error(new IllegalArgumentException("Failed to retrieve PR URL from thread"));
        }
        catch (RateLimitedException exception) {
            LOGGER.log(Level.WARNING, "Rate limited when retrieving message from thread: " + threadChannel.getId(), exception);
            return Result.error(new IllegalArgumentException("Failed to retrieve PR URL from thread"));
        }
        catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Unexpected error retrieving message from thread: " + threadChannel.getId(), exception);
            return Result.error(new IllegalArgumentException("Failed to retrieve PR URL from thread"));
        }
    }

    public CompletableFuture<Void> mentionReviewersOnAllReviewChannels(JDA jda) {
        return CompletableFuture.runAsync(() -> {
            Guild guild = jda.getGuildById(this.appConfig.guildId);

            if (guild == null) {
                LOGGER.warning("Guild not found with ID: " + this.appConfig.guildId);
                return;
            }

            List<CompletableFuture<Void>> channelFutures = new ArrayList<>();

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    Result<GitHubPullRequest, IllegalArgumentException> result =
                        this.getPullRequestFromThread(threadChannel);

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
            LOGGER.warning("Guild is null when checking for existing review post");
            return false;
        }

        try {
            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    Result<GitHubPullRequest, IllegalArgumentException> result =
                        this.getPullRequestFromThread(threadChannel);
                    
                    if (result.isOk() && url.equals(result.get().toUrl())) {
                        return true;
                    }
                }
            }
        }
        catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Error checking if review post exists", exception);
        }

        return false;
    }

    public CompletableFuture<Void> archiveMergedPullRequest(JDA jda) {
        return CompletableFuture.runAsync(() -> {
            Guild guild = jda.getGuildById(this.appConfig.guildId);

            if (guild == null) {
                LOGGER.warning("Guild not found with ID: " + this.appConfig.guildId);
                return;
            }

            AppConfig.ReviewSystem reviewSystem = this.appConfig.reviewSystem;
            List<CompletableFuture<Void>> archiveFutures = new ArrayList<>();

            for (ForumChannel forumChannel : guild.getForumChannels()) {
                for (ThreadChannel threadChannel : forumChannel.getThreadChannels()) {
                    Result<GitHubPullRequest, IllegalArgumentException> result = this.getPullRequestFromThread(threadChannel);

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

                            if (isMerged) {
                                threadChannel.getManager()
                                    .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.mergedTagId))
                                    .setLocked(true)
                                    .setArchived(true)
                                    .queue(
                                        success -> LOGGER.info("Archived merged PR: " + pullRequest.toUrl()),
                                        failure -> LOGGER.log(
                                            Level.WARNING,
                                            "Failed to archive merged PR: " + pullRequest.toUrl(),
                                            failure)
                                    );
                            }
                            else if (isClosed) {
                                threadChannel.getManager()
                                    .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.closedTagId))
                                    .setLocked(true)
                                    .setArchived(true)
                                    .queue(
                                        success -> LOGGER.info("Archived closed PR: " + pullRequest.toUrl()),
                                        failure -> LOGGER.log(
                                            Level.WARNING,
                                            "Failed to archive closed PR: " + pullRequest.toUrl(),
                                            failure)
                                    );
                            }
                        }
                        catch (IOException exception) {
                            Sentry.captureException(exception);
                            LOGGER.log(
                                Level.WARNING,
                                "Error checking PR status for archival: " + pullRequest.toUrl(),
                                exception);
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
                LOGGER.warning("Interrupted while waiting for rate limit");
            }
        }

        this.lastGitHubApiCall = Instant.now();
    }
}
