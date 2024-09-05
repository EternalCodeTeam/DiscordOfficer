package com.eternalcode.discordapp.scheduler;

import java.time.Duration;

public interface Scheduler {

    void schedule(Runnable task, Duration delay);

    void schedule(Runnable task);

    void shutdown() throws InterruptedException;
}
