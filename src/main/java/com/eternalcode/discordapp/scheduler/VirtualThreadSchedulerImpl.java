package com.eternalcode.discordapp.scheduler;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualThreadSchedulerImpl implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualThreadSchedulerImpl.class);
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void schedule(Runnable task, Duration delay) {
        if (delay.isNegative() || delay.isZero()) {
            this.schedule(task);
            return;
        }

        this.executorService.submit(() -> {
            try {
                Thread.sleep(delay.toMillis());
                task.run();
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Task interrupted during delay", exception);
            }
            catch (Exception exception) {
                LOGGER.error("Task execution failed", exception);
            }
        });
    }

    @Override
    public void schedule(Runnable task) {
        this.executorService.submit(() -> {
            try {
                task.run();
            }
            catch (Exception exception) {
                LOGGER.error("Immediate task execution failed", exception);
            }
        });
    }

    @Override
    public void shutdown() {
        try {
            LOGGER.info("Initiating scheduler shutdown...");
            this.executorService.shutdown();

            if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.warn("Scheduler did not terminate within 60 seconds, forcing shutdown...");
                this.executorService.shutdownNow();
            }
            else {
                LOGGER.info("Scheduler shut down successfully.");
            }
        }
        catch (InterruptedException exception) {
            LOGGER.error("Shutdown interrupted", exception);
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
