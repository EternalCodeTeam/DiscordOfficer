package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardButtonController extends ListenerAdapter {

    private static final int PAGE_SIZE = 10;

    private final LeaderboardService leaderboardService;

    private final Map<Long, Integer> currentPageMap = new HashMap<>();

    public LeaderboardButtonController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        long messageId = event.getMessage().getIdLong();

        int totalRecords = this.leaderboardService.getTotalRecords();
        int totalPages = this.leaderboardService.getTotalPages(totalRecords);

        int currentPage = this.currentPageMap.getOrDefault(messageId, 1);

        if (componentId.equals("leaderboard_next")) {
            currentPage++;
        }

        if (componentId.equals("leaderboard_prev")) {
            currentPage--;
        }

        if (componentId.equals("leaderboard_first")) {
            currentPage = 1;
        }

        if (componentId.equals("leaderboard_last")) {
            currentPage = totalPages;
        }

        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        this.currentPageMap.put(messageId, currentPage);

        this.updateLeaderboard(event, currentPage);
    }

    private void updateLeaderboard(ButtonInteractionEvent event, int currentPage) {
        int totalRecords = this.leaderboardService.getTotalRecords();
        int totalPages = this.leaderboardService.getTotalPages(totalRecords);

        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalRecords);

        List<Level> top = this.leaderboardService.getLeaderboard(startIndex, endIndex);

        EmbedBuilder embedBuilder = this.leaderboardService.createLeaderboardEmbedBuilder(currentPage, totalPages);
        int index = startIndex + 1;
        StringBuilder leaderboardContent = new StringBuilder();

        for (Level level : top) {
            int userLevel = level.getCurrentLevel();
            Member memberById = event.getGuild().getMemberById(level.getId());

            if (memberById != null) {
                String effectiveName = memberById.getEffectiveName();

                leaderboardContent.append(this.leaderboardService.formatLeaderboardEntry(index, effectiveName, userLevel)).append("\n");
                index++;
            }
        }

        Button firstButton = Button.success("leaderboard_first", "First")
            .withEmoji(Emoji.fromUnicode("U+23EE"))
            .withDisabled(currentPage == 1);

        Button prevButton = Button.primary("leaderboard_prev", "Previous")
            .withEmoji(Emoji.fromFormatted("U+25C0"))
            .withDisabled(currentPage <= 1);

        Button nextButton = Button.primary("leaderboard_next", "Next")
            .withEmoji(Emoji.fromUnicode("U+25B6"))
            .withDisabled(currentPage >= totalPages);

        Button lastButton = Button.success("leaderboard_last", "Last")
            .withEmoji(Emoji.fromUnicode("U+23ED"))
            .withDisabled(currentPage == totalPages);

        event.editMessageEmbeds(embedBuilder.setDescription(leaderboardContent.toString()).build())
            .setActionRow(firstButton, prevButton, nextButton, lastButton)
            .queue();
    }
}
