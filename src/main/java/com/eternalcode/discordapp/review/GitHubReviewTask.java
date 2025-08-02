package com.eternalcode.discordapp.review;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.scheduler.Scheduler;
import io.sentry.Sentry;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.JDA;

public class GitHubReviewTask {

    private static final Logger LOGGER = Logger.getLogger(GitHubReviewTask.class.getName());
    private static final Duration OPERATION_TIMEOUT = Duration.ofMinutes(30);
    private static final Duration TASK_INTERVAL = Duration.ofMinutes(15);

    private final GitHubReviewService gitHubReviewService;
    private final JDA jda;
    private final Scheduler scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public GitHubReviewTask(GitHubReviewService gitHubReviewService, JDA jda, Scheduler scheduler) {
        this.gitHubReviewService = gitHubReviewService;
        this.jda = jda;
        this.scheduler = scheduler;
    }

    public void start() {
        if (this.isRunning.compareAndSet(false, true)) {
            LOGGER.info("Starting GitHub review task with interval: " + TASK_INTERVAL);
            this.scheduler.scheduleRepeating(this::executeTask, Duration.ofMinutes(1), TASK_INTERVAL);
        }
        else {
            LOGGER.warning("GitHub review task is already running");
        }
    }

    public void stop() {
        if (this.isRunning.compareAndSet(true, false)) {
            LOGGER.info("Stopping GitHub review task");
        }
    }

    private void executeTask() {
        if (!this.isRunning.get()) {
            return;
        }

        LOGGER.info("Starting GitHub review task execution");

        try {
            if (this.jda.getStatus() != JDA.Status.CONNECTED) {
                LOGGER.warning("JDA is not connected, skipping task execution. Status: " + this.jda.getStatus());
                return;
            }

            CompletableFuture<Void> archiveTask =
                CompletableFuture.runAsync(() -> this.scheduler.schedule(() -> this.gitHubReviewService.archiveMergedPullRequest(
                        this.jda)
                    .orTimeout(OPERATION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        if (throwable instanceof TimeoutException) {
                            LOGGER.log(Level.WARNING, "Archive task timed out after " + OPERATION_TIMEOUT);
                        }
                        else {
                            LOGGER.log(Level.SEVERE, "Error in archiveMergedPullRequest", throwable);
                        }
                        Sentry.captureException(throwable);
                        return null;
                    })
                    .join()));

            CompletableFuture<Void> mentionTask = CompletableFuture.runAsync(() -> this.scheduler.schedule(() -> {
                this.gitHubReviewService.mentionReviewersOnAllReviewChannels(this.jda)
                    .orTimeout(OPERATION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        if (throwable instanceof TimeoutException) {
                            LOGGER.log(Level.WARNING, "Mention task timed out after " + OPERATION_TIMEOUT);
                        }
                        else {
                            LOGGER.log(Level.SEVERE, "Error in mentionReviewersOnAllReviewChannels", throwable);
                        }
                        Sentry.captureException(throwable);
                        return null;
                    })
                    .join();
            }));

            CompletableFuture.allOf(archiveTask, mentionTask)
                .whenComplete(FutureHandler.whenSuccess(result ->
                    LOGGER.info("GitHub review task completed successfully")
                ))
                .exceptionally(FutureHandler::handleException)
                .join();
        }
        catch (Exception exception) {
            Sentry.captureException(exception);
            LOGGER.log(Level.SEVERE, "Unexpected error in GitHubReviewTask", exception);
        }
    }
}
