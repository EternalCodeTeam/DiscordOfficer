package com.eternalcode.discordapp.feature.review.database;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.feature.review.GitHubPullRequest;
import com.eternalcode.discordapp.feature.review.GitHubReviewMention;
import com.eternalcode.discordapp.feature.review.GitHubReviewStatus;
import com.eternalcode.commons.scheduler.loom.LoomScheduler;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitHubReviewMentionRepositoryImpl extends AbstractRepository<GitHubReviewMentionWrapper, String>
    implements GitHubReviewMentionRepository {

    private static final Logger LOGGER = Logger.getLogger(GitHubReviewMentionRepositoryImpl.class.getName());
    private static final Duration MENTION_INTERVAL = Duration.ofHours(12);
    private static final Duration CLEANUP_INTERVAL = Duration.ofDays(7);

    private final LoomScheduler scheduler;

    public GitHubReviewMentionRepositoryImpl(DatabaseManager databaseManager, LoomScheduler scheduler) {
        super(databaseManager, GitHubReviewMentionWrapper.class);
        this.scheduler = scheduler;

        this.scheduler.runAsyncTimer(
            this::performCleanup,
            Duration.ofHours(1),
            Duration.ofHours(24)
        );
    }

    public static GitHubReviewMentionRepository create(DatabaseManager databaseManager, LoomScheduler scheduler) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), GitHubReviewMentionWrapper.class);
            LOGGER.info("GitHubReviewMentionRepository initialized successfully");
        } catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new DataAccessException("Failed to create github_review_mentions table", sqlException);
        }

        return new GitHubReviewMentionRepositoryImpl(databaseManager, scheduler);
    }

    @Override
    public CompletableFuture<Void> markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId, long threadId) {
        if (pullRequest == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("PullRequest cannot be null"));
        }

        return this.scheduler.<Void>supplyAsync(() -> {
            try {
                String mentionKey = this.createMentionKey(pullRequest, userId);
                GitHubReviewMentionWrapper mention = this.select(mentionKey).join()
                    .orElse(new GitHubReviewMentionWrapper(
                        mentionKey,
                        userId,
                        Instant.now().toEpochMilli(),
                        GitHubReviewStatus.PENDING.name(),
                        threadId,
                        0
                    ));

                mention.setLastMention(Instant.now());
                mention.setReviewStatus(GitHubReviewStatus.PENDING);
                mention.setThreadId(threadId);
                mention.setLastReminderSent(null);

                this.save(mention).join();
                LOGGER.info("Marked reviewer as mentioned: userId=" + userId + ", PR=" + pullRequest.toUrl());
                return null;
            } catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error marking reviewer as mentioned", exception);
                throw new DataAccessException("Failed to mark reviewer as mentioned", exception);
            }
        }).toCompletableFuture().exceptionally(FutureHandler::handleException);
    }

    @Override
    public CompletableFuture<Boolean> isMentioned(GitHubPullRequest pullRequest, long userId) {
        if (pullRequest == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.scheduler.supplyAsync(() -> {
            try {
                String mentionKey = this.createMentionKey(pullRequest, userId);
                GitHubReviewMentionWrapper mention = this.select(mentionKey).join().orElse(null);

                if (mention == null) {
                    return false;
                }

                Instant lastMention = mention.getLastMention();
                Instant nextMention = lastMention.plus(MENTION_INTERVAL);
                boolean isMentioned = nextMention.isAfter(Instant.now());

                LOGGER.fine("Checked mention status: userId=" + userId + ", PR=" + pullRequest.toUrl() + ", mentioned="
                    + isMentioned);
                return isMentioned;
            } catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error checking if user is mentioned", exception);
                return false;
            }
        }).toCompletableFuture().exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.log(Level.SEVERE, "Exception in isMentioned", throwable);
            return false;
        });
    }

    @Override
    public CompletableFuture<Void> recordReminderSent(GitHubPullRequest pullRequest, long userId) {
        if (pullRequest == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("PullRequest cannot be null"));
        }

        return this.scheduler.<Void>supplyAsync(() -> {
            try {
                String mentionKey = this.createMentionKey(pullRequest, userId);
                GitHubReviewMentionWrapper mention = this.select(mentionKey).join().orElse(null);

                if (mention != null) {
                    mention.setLastReminderSent(Instant.now());
                    this.save(mention).join();
                    LOGGER.info("Recorded reminder sent: userId=" + userId + ", PR=" + pullRequest.toUrl());
                } else {
                    LOGGER.warning("Mention not found when recording reminder: userId=" + userId + ", PR="
                        + pullRequest.toUrl());
                }
                return null;
            } catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error recording reminder sent", exception);
                throw new DataAccessException("Failed to record reminder sent", exception);
            }
        }).toCompletableFuture().exceptionally(FutureHandler::handleException);
    }

    @Override
    public List<ReviewerReminder> getReviewersNeedingReminders(Duration reminderInterval) {
        List<ReviewerReminder> reminders = new ArrayList<>();

        try {
            Instant now = Instant.now();
            Instant cutoffTime = now.minus(reminderInterval);
            long cutoffTimeMillis = cutoffTime.toEpochMilli();

            List<GitHubReviewMentionWrapper> filteredMentions =
                this.databaseManager.getDao(GitHubReviewMentionWrapper.class)
                    .queryBuilder()
                    .where()
                    .eq("reviewStatus", GitHubReviewStatus.PENDING.name())
                    .and()
                    .raw("lastMention < " + cutoffTimeMillis)
                    .and()
                    .raw("(lastReminderSent IS NULL OR lastReminderSent = 0 OR lastReminderSent < "
                        + cutoffTimeMillis + ")")
                    .query();

            for (GitHubReviewMentionWrapper mention : filteredMentions) {
                try {
                    reminders.add(new ReviewerReminder(
                        mention.getUserId(),
                        mention.getPullRequestUrl(),
                        mention.getThreadId()
                    ));
                } catch (Exception exception) {
                    LOGGER.log(
                        Level.WARNING,
                        "Error creating reminder for mention: " + mention.getPullRequestUrl(),
                        exception);
                }
            }

            LOGGER.info("Found " + reminders.size() + " reviewers needing reminders");
        } catch (SQLException exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "Database error getting reviewers needing reminders", exception);
            throw new DataAccessException("Failed to get reviewers needing reminders", exception);
        }

        return reminders;
    }

    @Override
    public CompletableFuture<GitHubReviewMention> find(String pullRequest, long userId) {
        if (pullRequest == null || pullRequest.trim().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return this.scheduler.supplyAsync(() -> {
            try {
                String mentionKey = this.createMentionKey(pullRequest, userId);
                GitHubReviewMentionWrapper wrapper = this.select(mentionKey).join().orElse(null);
                return wrapper == null ? null : wrapper.toMention();
            } catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Database error finding review mention", exception);
                throw new DataAccessException("Failed to find review mention", exception);
            }
        }).toCompletableFuture().exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.log(Level.SEVERE, "Exception in find", throwable);
            return null;
        });
    }

    @Override
    public void updateReviewStatus(GitHubPullRequest pullRequest, GitHubReviewStatus status) {
        if (pullRequest == null || status == null) {
            return;
        }

        try {
            UpdateBuilder<GitHubReviewMentionWrapper, Object> updateBuilder =
                this.databaseManager.getDao(GitHubReviewMentionWrapper.class).updateBuilder();

            updateBuilder.updateColumnValue("reviewStatus", status.name());
            updateBuilder.where().like("pullRequest", pullRequest.toUrl() + "|%");

            updateBuilder.update();
        } catch (SQLException exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "Error updating review status", exception);
            throw new DataAccessException("Failed to update review status", exception);
        }
    }

    private String createMentionKey(GitHubPullRequest pullRequest, long userId) {
        return pullRequest.toUrl() + "|" + userId;
    }

    private String createMentionKey(String pullRequestUrl, long userId) {
        return pullRequestUrl + "|" + userId;
    }

    public CompletableFuture<Integer> cleanupOldMentions(Duration maxAge) {
        return this.scheduler.supplyAsync(() -> {
            try {
                Instant cutoffTime = Instant.now().minus(maxAge);
                long cutoffTimeMillis = cutoffTime.toEpochMilli();

                DeleteBuilder<GitHubReviewMentionWrapper, Object> deleteBuilder =
                    this.databaseManager.getDao(GitHubReviewMentionWrapper.class).deleteBuilder();

                deleteBuilder.where()
                    .lt("lastMention", cutoffTimeMillis)
                    .and()
                    .in("reviewStatus", GitHubReviewStatus.MERGED.name(), GitHubReviewStatus.CLOSED.name());

                int deletedCount = deleteBuilder.delete();

                if (deletedCount > 0) {
                    LOGGER.info("Cleaned up " + deletedCount + " old mentions");
                }

                return deletedCount;
            } catch (SQLException exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error cleaning up old mentions", exception);
                throw new DataAccessException("Failed to cleanup old mentions", exception);
            }
        }).toCompletableFuture().exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.log(Level.SEVERE, "Exception in cleanupOldMentions", throwable);
            return 0;
        });
    }

    private void performCleanup() {
        try {
            this.cleanupOldMentions(CLEANUP_INTERVAL)
                .exceptionally(FutureHandler::handleException);
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Error during periodic cleanup", exception);
        }
    }
}

