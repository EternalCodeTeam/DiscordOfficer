package com.eternalcode.discordapp.feature.leveling.leaderboard;

import com.eternalcode.discordapp.feature.leveling.Level;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.List;

public class LeaderboardCommand extends SlashCommand {

    private static final int PAGE_SIZE = 10;

    private final LeaderboardService leaderboardService;

    public LeaderboardCommand(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;

        this.name = "leaderboard";
        this.help = "Shows the top users in level ranking";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        int totalRecords = this.leaderboardService.getTotalRecords();

        int totalPages = this.leaderboardService.getTotalPages(totalRecords);
        int page = 1;

        int startIndex = 0;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalRecords);
        List<Level> top = this.leaderboardService.getLeaderboard(startIndex, endIndex);

        EmbedBuilder embedBuilder = this.leaderboardService.createLeaderboardEmbedBuilder(page, totalPages);

        if (top.isEmpty()) {
            event.replyEmbeds(embedBuilder.setDescription("The leaderboard is empty.").build()).queue();
            return;
        }

        StringBuilder leaderboardContent = new StringBuilder();
        int index = startIndex + 1;

        for (Level level : top) {
            int userLevel = level.getCurrentLevel();
            Guild guild = event.getGuild();

            if (guild == null) {
                continue;
            }

            Member member = guild.getMemberById(level.getId());

            if (member == null) {
                continue;
            }

            String effectiveName = member.getEffectiveName();

            leaderboardContent.append(this.leaderboardService.formatLeaderboardEntry(index, effectiveName, userLevel)).append("\n");
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
                .setComponents(ActionRow.of(firstButton, prevButton, nextButton, lastButton))
                .queue();
    }
}

