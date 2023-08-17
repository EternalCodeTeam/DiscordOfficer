package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import com.eternalcode.discordapp.leveling.LevelService;

import java.util.List;

public class LeaderboardService {

    private final LevelService levelService;

    public LeaderboardService(LevelService levelService) {
        this.levelService = levelService;
    }

    public List<Level> getLeaderboard(int start, int end) {
        int pageSize = end - start + 1;
        int offset = start - 1;

        return this.levelService.getTop(pageSize, offset).join();
    }
}
