package com.eternalcode.discordapp.scheduler;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualThreadSchedulerImpl implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualThreadSchedulerImpl.class);

    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService virtualExecutor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public VirtualThreadSchedulerImpl() {
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            2,
            Thread.ofPlatform()
                .name("scheduler-", 0)
                .daemon(true)
                .factory()
        );

        this.virtualExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual()
                .name("task-", 0)
                .factory()
        );
    }

    @Override
    public void schedule(Runnable task, Duration delay) {
        if (isShutdown.get()) {
            LOGGER.warn("Scheduler is shutdown, ignoring task scheduling");
            return;
        }

        if (delay.isNegative()) {
            throw new IllegalArgumentException("Delay cannot be negative");
        }

        if (delay.isZero()) {
            schedule(task);
            return;
        }

        scheduledExecutor.schedule(
            () -> {
                if (!isShutdown.get()) {
                    virtualExecutor.submit(wrapTask(task, "delayed"));
                }
            }, delay.toMillis(), TimeUnit.MILLISECONDS);

        LOGGER.debug("Scheduled task with delay: {}", delay);
    }

    @Override
    public void schedule(Runnable task) {
        if (isShutdown.get()) {
            LOGGER.warn("Scheduler is shutdown, ignoring task scheduling");
            return;
        }

        virtualExecutor.submit(wrapTask(task, "immediate"));
        LOGGER.debug("Scheduled immediate task");
    }

    @Override
    public void scheduleRepeating(Runnable task, Duration interval) {
        scheduleRepeating(task, Duration.ZERO, interval);
    }

    @Override
    public void scheduleRepeating(Runnable task, Duration initialDelay, Duration interval) {
        if (isShutdown.get()) {
            LOGGER.warn("Scheduler is shutdown, ignoring repeating task scheduling");
            return;
        }

        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("Interval must be positive");
        }

        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException("Initial delay cannot be negative");
        }

        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(
            () -> {
                if (!isShutdown.get()) {
                    virtualExecutor.submit(wrapTask(task, "repeating"));
                }
            },
            initialDelay.toMillis(),
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        );

        LOGGER.debug(
            "Scheduled repeating task with initial delay: {} and interval: {}",
            initialDelay, interval);

        virtualExecutor.submit(() -> {
            try {
                while (!isShutdown.get()) {
                    Thread.sleep(Duration.ofSeconds(1));
                }
                future.cancel(false);
                LOGGER.debug("Cancelled repeating task due to shutdown");
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                future.cancel(true);
            }
        });
    }

    @Override
    public void shutdown() throws InterruptedException {
        if (!isShutdown.compareAndSet(false, true)) {
            LOGGER.warn("Scheduler already shutdown");
            return;
        }

        LOGGER.info("Initiating scheduler shutdown...");

        try {
            scheduledExecutor.shutdown();

            virtualExecutor.shutdown();

            if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.warn("Scheduled executor did not terminate within 30 seconds, forcing shutdown");
                scheduledExecutor.shutdownNow();

                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.error("Scheduled executor did not terminate after forced shutdown");
                }
            }

            if (!virtualExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.warn("Virtual executor did not terminate within 30 seconds, forcing shutdown");
                virtualExecutor.shutdownNow();

                if (!virtualExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.error("Virtual executor did not terminate after forced shutdown");
                }
            }

            LOGGER.info("Scheduler shutdown completed successfully");
        }
        catch (InterruptedException exception) {
            LOGGER.error("Shutdown interrupted, forcing immediate shutdown", exception);
            scheduledExecutor.shutdownNow();
            virtualExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            throw exception;
        }
    }

    private Runnable wrapTask(Runnable task, String taskType) {
        return () -> {
            long startTime = System.nanoTime();
            String threadName = Thread.currentThread().getName();

            try {
                LOGGER.trace("Starting {} task on thread: {}", taskType, threadName);
                task.run();

                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                LOGGER.trace("Completed {} task on thread: {} in {}ms", taskType, threadName, durationMs);
            }
            catch (Exception exception) {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                LOGGER.error("Task failed on thread: {} after {}ms", threadName, durationMs, exception);
            }
        };
    }
}
