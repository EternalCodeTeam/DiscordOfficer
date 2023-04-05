package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import panda.std.Option;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReviewRepositoryImpl extends AbstractRepository<ReviewWrapper, Long> implements ReviewRepository {

    protected ReviewRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, ReviewWrapper.class);
    }

    public static ReviewRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), ReviewWrapper.class);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return new ReviewRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Review> find(long id) {
        return this.select(id).thenApply(reviewWrapperOptional -> reviewWrapperOptional
                .map(ReviewWrapper::toReview)
                .orElse(null)
        );
    }

    @Override
    public CompletableFuture<Review> findById(long id) {
        return this.select(id).thenApply(reviewWrapperOptional -> reviewWrapperOptional
                .map(ReviewWrapper::toReview)
                .orElse(null)
        );
    }

    @Override
    public CompletableFuture<Review> findByPullRequestUrl(String url) {
        return this.action(dao -> {
            List<ReviewWrapper> reviewWrappers = dao.queryForEq("pullRequestUrl", url);

            if (reviewWrappers.isEmpty()) {
                return null;
            }

            return reviewWrappers.get(0).toReview();
        });
    }

    @Override
    public CompletableFuture<Review> findByPullRequestTitle(String title) {
        return this.action(dao -> {
            List<ReviewWrapper> reviewWrappers = dao.queryForEq("pullRequestTitle", title);

            if (reviewWrappers.isEmpty()) {
                return null;
            }

            return reviewWrappers.get(0).toReview();
        });
    }


    @Override
    public CompletableFuture<Dao.CreateOrUpdateStatus> saveReview(Review review) {
        return this.save(ReviewWrapper.from(review));
    }

    @Override
    public CompletableFuture<Review> deleteReview(Review review) {
        return this.delete(ReviewWrapper.from(review)).thenApply(url -> null);
    }

    @Override
    public CompletableFuture<Review> deleteReviewById(long id) {
        return this.deleteById(id).thenApply(url -> null);
    }

    @Override
    public CompletableFuture<Review> deleteReviewByPullRequestUrl(String url) {
        return this.action(dao -> {
            List<ReviewWrapper> reviewWrappers = dao.queryForEq("pullRequestUrl", url);

            if (reviewWrappers.isEmpty()) {
                return null;
            }

            dao.delete(reviewWrappers.get(0));
            return reviewWrappers.get(0).toReview();
        });
    }

    @Override
    public CompletableFuture<Review> deleteReviewByPullRequestTitle(String title) {
        return this.action(dao -> {
            List<ReviewWrapper> reviewWrappers = dao.queryForEq("pullRequestTitle", title);

            if (reviewWrappers.isEmpty()) {
                return null;
            }

            dao.delete(reviewWrappers.get(0));
            return reviewWrappers.get(0).toReview();
        });
    }

}
