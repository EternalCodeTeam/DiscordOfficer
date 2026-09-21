package com.eternalcode.discordapp.feature.releasewatch;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.scheduler.Scheduler;
import io.sentry.Sentry;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseWatchTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseWatchTask.class);
    private static final Duration INITIAL_DELAY = Duration.ofMinutes(1);
    private static final Duration POLL_INTERVAL = Duration.ofMinutes(5);
    private static final Duration OPERATION_TIMEOUT = Duration.ofMinutes(5);

    private final ReleaseWatchService releaseWatchService;
    private final JDA jda;
    private final Scheduler scheduler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ReleaseWatchTask(ReleaseWatchService releaseWatchService, JDA jda, Scheduler scheduler) {
        this.releaseWatchService = releaseWatchService;
        this.jda = jda;
        this.scheduler = scheduler;
    }

    public void start() {
        if (this.isRunning.compareAndSet(false, true)) {
            LOGGER.info("Starting release watch task with interval: {}", POLL_INTERVAL);
            this.scheduler.scheduleRepeating(this::executeTask, INITIAL_DELAY, POLL_INTERVAL);
        }
    }

    private void executeTask() {
        if (!this.isRunning.get() || this.jda.getStatus() != JDA.Status.CONNECTED) {
            return;
        }

        this.releaseWatchService.pollForNewReleases(this.jda)
            .orTimeout(OPERATION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            .exceptionally(throwable -> {
                if (throwable instanceof TimeoutException) {
                    LOGGER.warn("Release watch poll timed out after {}", OPERATION_TIMEOUT);
                }
                else {
                    LOGGER.error("Error polling release watches", throwable);
                }
                Sentry.captureException(throwable);
                return null;
            })
            .exceptionally(FutureHandler::handleException)
            .join();
    }
}
