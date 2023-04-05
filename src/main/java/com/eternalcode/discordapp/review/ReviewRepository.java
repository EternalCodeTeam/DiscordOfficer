package com.eternalcode.discordapp.review;

import com.eternalcode.discordapp.experience.Experience;
import com.j256.ormlite.dao.Dao;

import java.util.concurrent.CompletableFuture;

public interface ReviewRepository {

    CompletableFuture<Review> find(long id);

    CompletableFuture<Review> findById(long id);

    CompletableFuture<Review> findByPullRequestUrl(String url);

    CompletableFuture<Review> findByPullRequestTitle(String title);

    CompletableFuture<Dao.CreateOrUpdateStatus> saveReview(Review review);

    CompletableFuture<Review> deleteReview(Review review);

    CompletableFuture<Review> deleteReviewById(long id);

    CompletableFuture<Review> deleteReviewByPullRequestUrl(String url);

    CompletableFuture<Review> deleteReviewByPullRequestTitle(String title);

}
