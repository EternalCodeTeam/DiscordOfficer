package com.eternalcode.discordapp.feature.guildstats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuildStatisticsTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuildStatisticsTask.class);

    private final GuildStatisticsService guildStatisticsService;

    public GuildStatisticsTask(GuildStatisticsService guildStatisticsService) {
        this.guildStatisticsService = guildStatisticsService;
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Starting guild statistics update task");
            guildStatisticsService.displayStats().join();
            LOGGER.debug("Guild statistics update task completed");
        }
        catch (Exception exception) {
            LOGGER.error("Error during guild statistics update: {}", exception.getMessage(), exception);
        }
    }
}
