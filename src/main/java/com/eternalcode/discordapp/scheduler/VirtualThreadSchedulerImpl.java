package com.eternalcode.discordapp.scheduler;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualThreadSchedulerImpl implements Scheduler {

    // works on javca 21+
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualThreadSchedulerImpl.class.getName());

    @Override
    public void schedule(Runnable task, Duration delay) {
        EXECUTOR_SERVICE.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                task.run();

                try {
                    Thread.sleep(delay.toMillis());
                }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public void schedule(Runnable task) {
        EXECUTOR_SERVICE.submit(task);
    }

    @Override
    public void shutdown() {
        try {
            LOGGER.info("Shutting down executor service...");
            EXECUTOR_SERVICE.shutdown();

            if (!EXECUTOR_SERVICE.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.warn("Executor did not terminate in the specified time.");
                EXECUTOR_SERVICE.shutdownNow();
            }

            LOGGER.info("Executor service shut down successfully.");
        }
        catch (InterruptedException exception) {
            LOGGER.error("Shutdown interrupted", exception);
            EXECUTOR_SERVICE.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
