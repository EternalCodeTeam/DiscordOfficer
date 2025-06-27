package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepository;
import io.sentry.Sentry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import java.io.IOException;

public class GitHubReviewReminderService {

    private static final Logger LOGGER = Logger.getLogger(GitHubReviewReminderService.class.getName());
    private static final Duration DEFAULT_REMINDER_INTERVAL = Duration.ofHours(24);

    private final JDA jda;
    private final GitHubReviewMentionRepository mentionRepository;
    private final ScheduledExecutorService scheduler;
    private final Duration reminderInterval;
    private final AppConfig appConfig;

    public GitHubReviewReminderService(
        JDA jda,
        GitHubReviewMentionRepository mentionRepository,
        AppConfig appConfig,
        Duration reminderInterval) {
        this.jda = jda;
        this.mentionRepository = mentionRepository;
        this.reminderInterval = reminderInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.appConfig = appConfig;
    }

    public GitHubReviewReminderService(JDA jda, GitHubReviewMentionRepository mentionRepository, AppConfig appConfig) {
        this(jda, mentionRepository, appConfig, DEFAULT_REMINDER_INTERVAL);
    }

    public void start() {
        this.scheduler.scheduleAtFixedRate(this::sendReminders, 1, this.reminderInterval.toMinutes(), TimeUnit.MINUTES);
        LOGGER.info("GitHub review reminder service started");
    }

    public void stop() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                this.scheduler.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            this.scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("GitHub review reminder service stopped");
    }

    private void sendReminders() {
        try {
            this.mentionRepository.getReviewersNeedingReminders(this.reminderInterval)
                .thenAccept(this::processReminders)
                .exceptionally(throwable -> {
                    Sentry.captureException(throwable);
                    LOGGER.log(Level.SEVERE, "Error sending reminders", throwable);
                    return null;
                });
        }
        catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "Error scheduling reminders", exception);
        }
    }

    private void processReminders(List<GitHubReviewMentionRepository.ReviewerReminder> reminders) {
        for (GitHubReviewMentionRepository.ReviewerReminder reminder : reminders) {
            this.sendReminder(reminder);
        }
    }

    private void sendReminder(GitHubReviewMentionRepository.ReviewerReminder reminder) {
        long userId = reminder.userId();
        String pullRequestUrl = reminder.pullRequestUrl();
        long threadId = reminder.threadId();

        GitHubPullRequest pullRequest = GitHubPullRequest.fromUrl(pullRequestUrl).orNull();
        if (pullRequest == null) {
            LOGGER.warning("Invalid pull request URL: " + pullRequestUrl);
            return;
        }

        boolean isMerged = false;
        boolean isClosed = false;
        try {
            isMerged = GitHubReviewUtil.isPullRequestMerged(pullRequest, this.appConfig.githubToken);
            isClosed = GitHubReviewUtil.isPullRequestClosed(pullRequest, this.appConfig.githubToken);
        } catch (IOException e) {
            Sentry.captureException(e);
            LOGGER.log(Level.SEVERE, "Error checking PR status", e);
            return;
        }

        if (isMerged || isClosed) {
            ThreadChannel thread = this.jda.getThreadChannelById(threadId);
            if (thread != null) {
                AppConfig.ReviewSystem reviewSystem = this.appConfig.reviewSystem;
                if (isMerged) {
                    thread.getManager()
                        .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.mergedTagId))
                        .setLocked(true)
                        .setArchived(true)
                        .queue();
                } else if (isClosed) {
                    thread.getManager()
                        .setAppliedTags(ForumTagSnowflake.fromId(reviewSystem.closedTagId))
                        .setLocked(true)
                        .setArchived(true)
                        .queue();
                }
            }
            LOGGER.info("PR is merged or closed, skipping reminder for thread: " + threadId);
            return;
        }

        this.jda.retrieveUserById(userId).queue(
            user -> {
                if (user == null) {
                    LOGGER.warning("Could not find user with ID " + userId);
                    return;
                }

                ThreadChannel thread = this.jda.getThreadChannelById(threadId);
                if (thread == null) {
                    LOGGER.warning("Could not find thread with ID " + threadId);
                    return;
                }

                this.sendReminderMessage(user, thread, pullRequestUrl);
            }, throwable -> {
                Sentry.captureException(throwable);
                LOGGER.log(Level.SEVERE, "Error retrieving user", throwable);
            });
    }

    private void sendReminderMessage(User user, ThreadChannel thread, String pullRequestUrl) {
        String message = String.format(
            "Hey %s! you have been assigned as a reviewer for this pull request: <%s>.",
            user.getAsMention(),
            pullRequestUrl
        );

        thread.sendMessage(message).queue(
            success -> {
                GitHubPullRequest pullRequest = GitHubPullRequest.fromUrl(pullRequestUrl).orNull();
                if (pullRequest != null) {
                    this.mentionRepository.recordReminderSent(pullRequest, user.getIdLong());
                }
            },
            throwable -> {
                Sentry.captureException(throwable);
                LOGGER.log(Level.SEVERE, "Error sending reminder message", throwable);
            }
        );
    }
} 
