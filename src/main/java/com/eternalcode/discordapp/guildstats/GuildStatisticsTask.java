package com.eternalcode.discordapp.guildstats;

import java.util.TimerTask;

public class GuildStatisticsTask implements Runnable {

    private final GuildStatisticsService guildStatisticsService;

    public GuildStatisticsTask(GuildStatisticsService guildStatisticsService) {
        this.guildStatisticsService = guildStatisticsService;
    }

    @Override
    public void run() {
        this.guildStatisticsService.displayStats();
    }

}
