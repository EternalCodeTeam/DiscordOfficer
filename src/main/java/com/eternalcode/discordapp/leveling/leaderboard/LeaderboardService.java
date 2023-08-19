package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import com.eternalcode.discordapp.leveling.LevelService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class LeaderboardService {

    private static final int PAGE_SIZE = 10;

    private final LevelService levelService;

    public LeaderboardService(LevelService levelService) {
        this.levelService = levelService;
    }

    public List<Level> getLeaderboard(int start, int end) {
        int pageSize = end - start;

        return this.levelService.getTop(pageSize, start).join();
    }

    public EmbedBuilder createEmbedBuilder(int currentPage, int totalPages) {
        return new EmbedBuilder()
                .setTitle("👑 Leaderboard")
                .setColor(Color.RED)
                .setFooter(String.format("Page %d/%d", currentPage, totalPages));
    }

    public String formatLeaderboardEntry(int index, String userId, int userLevel) {
        return String.format("**%d.** %s - **LVL**: `%d`", index, userId, userLevel);
    }

    public int getTotalPages(int totalRecords) {
        return (int) Math.ceil((double) totalRecords / PAGE_SIZE);
    }

    public int getTotalRecords() {
        return this.levelService.getTotalRecordsCount().join();
    }

}
