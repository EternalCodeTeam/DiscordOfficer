package com.eternalcode.discordapp.guildstats;

import java.util.TimerTask;

public class GuildStatisticsTask extends TimerTask {

    private final GuildStatisticsService guildStatisticsService;

    public GuildStatisticsTask(GuildStatisticsService guildStatisticsService) {
        this.guildStatisticsService = guildStatisticsService;
    }

    @Override
    public void run() {
        this.guildStatisticsService.displayStats();
    }

}
