package com.eternalcode.discordapp.scheduler;

import java.time.Duration;

public interface Scheduler {

    void schedule(Runnable task, Duration delay);

    void schedule(Runnable task);

    void scheduleRepeating(Runnable task, Duration interval);

    void scheduleRepeating(Runnable task, Duration initialDelay, Duration interval);

    void shutdown() throws InterruptedException;
}
