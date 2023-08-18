package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.List;

public class LeaderboardCommand extends SlashCommand {

    private static final int PAGE_SIZE = 10;
    private final LeaderboardService leaderboardService;
    private final LeaderboardConfiguration leaderboardConfiguration;

    public LeaderboardCommand(LeaderboardService leaderboardService, LeaderboardConfiguration leaderboardConfiguration) {
        this.leaderboardService = leaderboardService;
        this.leaderboardConfiguration = leaderboardConfiguration;

        this.name = "leaderboard";
        this.help = String.format("Shows the top %s users in level ranking", this.leaderboardConfiguration.records);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        int totalRecords = this.leaderboardConfiguration.records;
        int totalPages = this.leaderboardService.getTotalPages();
        int page = 1;

        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalRecords);
        List<Level> top = this.leaderboardService.getLeaderboard(startIndex, endIndex);

        EmbedBuilder embedBuilder = this.leaderboardService.createEmbedBuilder(page, totalPages);

        if (top.isEmpty()) {
            event.replyEmbeds(embedBuilder.setDescription("The leaderboard is empty.").build()).queue();
            return;
        }

        StringBuilder leaderboardContent = new StringBuilder();
        int index = startIndex + 1;

        for (Level level : top) {
            int userLevel = level.getLevel();
            leaderboardContent.append(this.leaderboardService.formatLeaderboardEntry(index, event.getGuild().getMemberById(level.getId()).getEffectiveName(), userLevel)).append("\n");
            index++;
        }

        Button firstButton = Button.success("leaderboard_first", "First")
                .withEmoji(Emoji.fromUnicode("U+23EE"))
                .withDisabled(page == 1);

        Button prevButton = Button.primary("leaderboard_prev", "Previous")
                .withEmoji(Emoji.fromFormatted("U+25C0"))
                .withDisabled(page <= 1);

        Button nextButton = Button.primary("leaderboard_next", "Next")
                .withEmoji(Emoji.fromUnicode("U+25B6"))
                .withDisabled(page >= totalPages);

        Button lastButton = Button.success("leaderboard_last", "Last")
                .withEmoji(Emoji.fromUnicode("U+23ED"))
                .withDisabled(page == totalPages);

        event.replyEmbeds(embedBuilder.setDescription(leaderboardContent.toString()).build())
                .addActionRow(firstButton, prevButton, nextButton, lastButton)
                .queue();
    }
}
