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
        return this.select(pullRequest.toUrl()).thenApply(mentionOptional -> {
            if (mentionOptional.isEmpty()) {
                return false;
            }

            GitHubReviewMentionWrapper mention = mentionOptional.get();
            Instant lastMention = mention.getLastMention();
            Instant nextMention = lastMention.plus(MENTION_INTERVAL);

            return nextMention.isAfter(Instant.now());
        });
    }

    @Override
    public CompletableFuture<Void> recordReminderSent(GitHubPullRequest pullRequest, long userId) {
        return CompletableFuture.runAsync(() -> {
            this.select(pullRequest.toUrl()).thenAccept(mentionOptional -> {
                if (mentionOptional.isPresent()) {
                    GitHubReviewMentionWrapper mention = mentionOptional.get();
                    mention.setLastReminderSent(Instant.now());
                    this.save(mention);
                }
            });
        });
    }

    @Override
    public CompletableFuture<List<ReviewerReminder>> getReviewersNeedingReminders(Duration reminderInterval) {
        return CompletableFuture.supplyAsync(() -> {
            List<ReviewerReminder> reminders = new ArrayList<>();
            Instant now = Instant.now();

            try {
                List<GitHubReviewMentionWrapper> allMentions =
                    this.databaseManager.getDao(GitHubReviewMentionWrapper.class).queryForAll();

                for (GitHubReviewMentionWrapper mention : allMentions) {
                    Instant lastReminderSent = mention.getLastReminderSent();

                    // If no reminder has been sent yet or the reminder interval has passed
                    if (lastReminderSent == null ||
                        lastReminderSent.plus(reminderInterval).isBefore(now)) {

                        reminders.add(new ReviewerReminder(
                            mention.getUserId(),
                            mention.getPullRequest(),
                            mention.getThreadId()
                        ));
                    }
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

                if (mentions.isEmpty()) {
                    return null;
                }

                return mentions.getFirst().toMention();
            }
            catch (SQLException e) {
                Sentry.captureException(e);
                throw new DataAccessException("Failed to find review mention", e);
            }
        });
    }
}
