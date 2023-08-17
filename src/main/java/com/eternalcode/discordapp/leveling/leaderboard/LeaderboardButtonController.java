package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import panda.utilities.text.Formatter;

import java.awt.Color;
import java.util.List;

public class LeaderboardButtonController extends ListenerAdapter {

    private static final int PAGE_SIZE = 10;
    private static int page = 1;
    private static int totalPages;

    private final LeaderboardConfiguration leaderboardConfiguration;
    private final LeaderboardService leaderboardService;

    public LeaderboardButtonController(LeaderboardConfiguration leaderboardConfiguration, LeaderboardService leaderboardService) {
        this.leaderboardConfiguration = leaderboardConfiguration;
        this.leaderboardService = leaderboardService;
        this.totalPages = (int) Math.ceil((double) leaderboardConfiguration.records / PAGE_SIZE);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("leaderboard_next") && page < totalPages) {
            page++;
            updateLeaderboard(event);
        } else if (event.getComponentId().equals("leaderboard_prev") && page > 1) {
            page--;
            updateLeaderboard(event);
        }
    }

    private void updateLeaderboard(ButtonInteractionEvent event) {
        int startIndex = (page - 1) * PAGE_SIZE;
        List<Level> top = leaderboardService.getLeaderboard(startIndex, PAGE_SIZE);

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(this.leaderboardConfiguration.embedSettings.title)
            .setColor(Color.decode(this.leaderboardConfiguration.embedSettings.color))
            .setFooter(String.format("Page %d/%d", page, totalPages), event.getUser().getAvatarUrl());

        if (top.isEmpty()) {
            embedBuilder.setDescription("The leaderboard is empty.");
            event.editMessageEmbeds(embedBuilder.build()).queue();
            return;
        }

        StringBuilder leaderboardContent = new StringBuilder();
        int index = startIndex + 1;

        for (Level level : top) {
            int userLevel = level.getLevel();

            Formatter formatter = new Formatter()
                .register("{index}", index)
                .register("{user}", event.getGuild().getMemberById(level.getId()).getEffectiveName())
                .register("{level}", userLevel);

            leaderboardContent.append(formatter.format("**{index}.** {user} - **LVL**: `{level}`")).append("\n");
            index++;
        }

        event.editMessageEmbeds(embedBuilder.setDescription(leaderboardContent.toString()).build()).queue();
    }

}
