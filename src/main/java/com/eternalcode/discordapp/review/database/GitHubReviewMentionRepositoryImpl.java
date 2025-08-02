package com.eternalcode.discordapp.review.database;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.review.GitHubPullRequest;
import com.eternalcode.discordapp.review.GitHubReviewMention;
import com.eternalcode.discordapp.review.GitHubReviewStatus;
import com.eternalcode.discordapp.scheduler.Scheduler;
import com.j256.ormlite.stmt.DeleteBuilder;
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

    private final Scheduler scheduler;

    public GitHubReviewMentionRepositoryImpl(DatabaseManager databaseManager, Scheduler scheduler) {
        super(databaseManager, GitHubReviewMentionWrapper.class);
        this.scheduler = scheduler;

        this.scheduler.scheduleRepeating(
            this::performCleanup,
            Duration.ofHours(1),
            Duration.ofHours(24)
        );
    }

    public static GitHubReviewMentionRepository create(DatabaseManager databaseManager, Scheduler scheduler) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), GitHubReviewMentionWrapper.class);
            LOGGER.info("GitHubReviewMentionRepository initialized successfully");
        }
        catch (SQLException sqlException) {
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

        return CompletableFuture.runAsync(() -> this.scheduler.schedule(() -> {
            try {
                String pullRequestKey = this.createPullRequestKey(pullRequest);

                GitHubReviewMentionWrapper existingMention = this.select(pullRequestKey).join().orElse(null);

                GitHubReviewMentionWrapper mention;
                if (existingMention != null) {
                    mention = new GitHubReviewMentionWrapper(
                        pullRequestKey,
                        userId,
                        Instant.now().toEpochMilli(),
                        GitHubReviewStatus.PENDING.name(),
                        threadId,
                        0
                    );
                }
                else {
                    mention = new GitHubReviewMentionWrapper(
                        pullRequestKey,
                        userId,
                        Instant.now().toEpochMilli(),
                        GitHubReviewStatus.PENDING.name(),
                        threadId,
                        0
                    );
                }

                this.save(mention);
                LOGGER.info("Marked reviewer as mentioned: userId=" + userId + ", PR=" + pullRequest.toUrl());
            }
            catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error marking reviewer as mentioned", exception);
                throw new DataAccessException("Failed to mark reviewer as mentioned", exception);
            }
        })).exceptionally(FutureHandler::handleException);
    }

    @Override
    public CompletableFuture<Boolean> isMentioned(GitHubPullRequest pullRequest, long userId) {
        if (pullRequest == null) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String pullRequestKey = this.createPullRequestKey(pullRequest);
                GitHubReviewMentionWrapper mention = this.select(pullRequestKey).join().orElse(null);

                if (mention == null) {
                    return false;
                }

                Instant lastMention = mention.getLastMention();
                Instant nextMention = lastMention.plus(MENTION_INTERVAL);
                boolean isMentioned = nextMention.isAfter(Instant.now());

                LOGGER.fine("Checked mention status: userId=" + userId + ", PR=" + pullRequest.toUrl() + ", mentioned="
                    + isMentioned);
                return isMentioned;
            }
            catch (Exception exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error checking if user is mentioned", exception);
                return false;
            }
        }).exceptionally(throwable -> {
            LOGGER.log(Level.SEVERE, "Exception in isMentioned", throwable);
            return false;
        });
    }

    @Override
    public CompletableFuture<Void> recordReminderSent(GitHubPullRequest pullRequest, long userId) {
        if (pullRequest == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("PullRequest cannot be null"));
        }

        return CompletableFuture.runAsync(() -> {
            this.scheduler.schedule(() -> {
                try {
                    String pullRequestKey = this.createPullRequestKey(pullRequest);
                    GitHubReviewMentionWrapper mention = this.select(pullRequestKey).join().orElse(null);

                    if (mention != null) {
                        mention.setLastReminderSent(Instant.now());
                        this.save(mention);
                        LOGGER.info("Recorded reminder sent: userId=" + userId + ", PR=" + pullRequest.toUrl());
                    }
                    else {
                        LOGGER.warning("Mention not found when recording reminder: userId=" + userId + ", PR="
                            + pullRequest.toUrl());
                    }
                }
                catch (Exception exception) {
                    Sentry.captureException(exception);
                    LOGGER.log(Level.SEVERE, "Error recording reminder sent", exception);
                    throw new DataAccessException("Failed to record reminder sent", exception);
                }
            });
        }).exceptionally(FutureHandler::handleException);
    }

    @Override
    public CompletableFuture<List<ReviewerReminder>> getReviewersNeedingReminders(Duration reminderInterval) {
        return CompletableFuture.supplyAsync(() -> {
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
                        .raw("(lastReminderSent IS NULL OR lastReminderSent = 0 OR lastReminderSent < "
                            + cutoffTimeMillis + ")")
                        .query();

                for (GitHubReviewMentionWrapper mention : filteredMentions) {
                    try {
                        reminders.add(new ReviewerReminder(
                            mention.getUserId(),
                            mention.getPullRequest(),
                            mention.getThreadId()
                        ));
                    }
                    catch (Exception exception) {
                        LOGGER.log(
                            Level.WARNING,
                            "Error creating reminder for mention: " + mention.getPullRequest(),
                            exception);
                    }
                }

                LOGGER.info("Found " + reminders.size() + " reviewers needing reminders");
            }
            catch (SQLException exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Database error getting reviewers needing reminders", exception);
                throw new DataAccessException("Failed to get reviewers needing reminders", exception);
            }

            return reminders;
        }).exceptionally(throwable -> {
            LOGGER.log(Level.SEVERE, "Exception in getReviewersNeedingReminders", throwable);
            return new ArrayList<>();
        });
    }

    @Override
    public CompletableFuture<GitHubReviewMention> find(String pullRequest, long userId) {
        if (pullRequest == null || pullRequest.trim().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<GitHubReviewMentionWrapper> mentions =
                    this.databaseManager.getDao(GitHubReviewMentionWrapper.class)
                        .queryBuilder()
                        .where()
                        .eq("pullRequest", pullRequest)
                        .and()
                        .eq("userId", userId)
                        .query();

                if (mentions.isEmpty()) {
                    return null;
                }

                GitHubReviewMentionWrapper wrapper = mentions.getFirst();
                return wrapper.toMention();
            }
            catch (SQLException exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Database error finding review mention", exception);
                throw new DataAccessException("Failed to find review mention", exception);
            }
        }).exceptionally(throwable -> {
            LOGGER.log(Level.SEVERE, "Exception in find", throwable);
            return null;
        });
    }

    private String createPullRequestKey(GitHubPullRequest pullRequest) {
        return pullRequest.toUrl();
    }

    public CompletableFuture<Integer> cleanupOldMentions(Duration maxAge) {
        return CompletableFuture.supplyAsync(() -> {
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
            }
            catch (SQLException exception) {
                Sentry.captureException(exception);
                LOGGER.log(Level.SEVERE, "Error cleaning up old mentions", exception);
                throw new DataAccessException("Failed to cleanup old mentions", exception);
            }
        }).exceptionally(throwable -> {
            LOGGER.log(Level.SEVERE, "Exception in cleanupOldMentions", throwable);
            return 0;
        });
    }

    private void performCleanup() {
        try {
            this.cleanupOldMentions(CLEANUP_INTERVAL)
                .exceptionally(FutureHandler::handleException);
        }
        catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Error during periodic cleanup", exception);
        }
    }
}
