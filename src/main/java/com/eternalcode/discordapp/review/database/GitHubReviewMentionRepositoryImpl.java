package com.eternalcode.discordapp.review.database;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.review.GitHubPullRequest;
import com.eternalcode.discordapp.review.GitHubReviewMention;
import com.eternalcode.discordapp.review.GitHubReviewStatus;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GitHubReviewMentionRepositoryImpl extends AbstractRepository<GitHubReviewMentionWrapper, String>
    implements GitHubReviewMentionRepository {

    private static final Duration MENTION_INTERVAL = Duration.ofHours(12);

    public GitHubReviewMentionRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, GitHubReviewMentionWrapper.class);
    }

    public static GitHubReviewMentionRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), GitHubReviewMentionWrapper.class);
        }
        catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new DataAccessException("Failed to create table", sqlException);
        }

        return new GitHubReviewMentionRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Void> markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId, long threadId) {
        return CompletableFuture.runAsync(() -> {
            GitHubReviewMentionWrapper mention =
                GitHubReviewMentionWrapper.create(
                    pullRequest.toUrl(),
                    userId,
                    Instant.now(),
                    GitHubReviewStatus.PENDING,
                    threadId);
            this.save(mention);
        });
    }

    @Override
    public CompletableFuture<Boolean> isMentioned(GitHubPullRequest pullRequest, long userId) {
        return this.select(pullRequest.toUrl())
            .thenApply(mentionOptional -> mentionOptional
                .map(mention -> {
                    Instant lastMention = mention.getLastMention();
                    Instant nextMention = lastMention.plus(MENTION_INTERVAL);
                    return nextMention.isAfter(Instant.now());
                })
                .orElse(false)
            );
    }

    @Override
    public CompletableFuture<Void> recordReminderSent(GitHubPullRequest pullRequest, long userId) {
        return this.select(pullRequest.toUrl())
            .thenCompose(mentionOptional -> {
                if (mentionOptional.isPresent()) {
                    GitHubReviewMentionWrapper mention = mentionOptional.get();
                    mention.setLastReminderSent(Instant.now());
                    return this.save(mention).thenApply(status -> null);
                }
                return CompletableFuture.completedFuture(null);
            });
    }

    @Override
    public CompletableFuture<List<ReviewerReminder>> getReviewersNeedingReminders(Duration reminderInterval) {
        return CompletableFuture.supplyAsync(() -> {
            List<ReviewerReminder> reminders = new ArrayList<>();
            Instant now = Instant.now();
            Instant cutoffTime = now.minus(reminderInterval);

            try {
                long cutoffTimeMillis = cutoffTime.toEpochMilli();
                
                List<GitHubReviewMentionWrapper> filteredMentions =
                    this.databaseManager.getDao(GitHubReviewMentionWrapper.class)
                        .queryBuilder()
                        .where()
                        .isNull("lastReminderSent")
                        .or()
                        .lt("lastReminderSent", cutoffTimeMillis)
                        .query();

                for (GitHubReviewMentionWrapper mention : filteredMentions) {
                    reminders.add(new ReviewerReminder(
                        mention.getUserId(),
                        mention.getPullRequest(),
                        mention.getThreadId()
                    ));
                }
            }
            catch (SQLException e) {
                Sentry.captureException(e);
                throw new DataAccessException("Failed to get reviewers needing reminders", e);
            }

            return reminders;
        });
    }

    @Override
    public CompletableFuture<GitHubReviewMention> find(String pullRequest, long userId) {
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

                return mentions.isEmpty() ? null : mentions.get(0).toMention();
            }
            catch (SQLException e) {
                Sentry.captureException(e);
                throw new DataAccessException("Failed to find review mention", e);
            }
        });
    }
}
