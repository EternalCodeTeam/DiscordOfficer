package com.eternalcode.discordapp.review.database;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.review.GitHubPullRequest;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
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
    public CompletableFuture<Void> markReviewerAsMentioned(GitHubPullRequest pullRequest, long userId) {
        return CompletableFuture.runAsync(() -> {
            GitHubReviewMentionWrapper mention =
                    GitHubReviewMentionWrapper.create(pullRequest.toUrl(), userId, Instant.now());
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
}
