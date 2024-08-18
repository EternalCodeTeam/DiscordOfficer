package com.eternalcode.discordapp.guildstats;

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
