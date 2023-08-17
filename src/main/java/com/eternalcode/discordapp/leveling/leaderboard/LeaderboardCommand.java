package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import panda.utilities.text.Formatter;

import java.awt.Color;
import java.util.List;

public class LeaderboardCommand extends SlashCommand {

    private static final int PAGE_SIZE = 10;
    private final LeaderboardConfiguration leaderboardConfiguration;
    private final LeaderboardService leaderboardService;

    public LeaderboardCommand(LeaderboardConfiguration leaderboardConfiguration, LeaderboardService leaderboardService) {
        this.leaderboardConfiguration = leaderboardConfiguration;
        this.leaderboardService = leaderboardService;

        this.name = "leaderboard";
        this.help = String.format("Shows the top %s users in level ranking", this.leaderboardConfiguration.records);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        int totalPages = (int) Math.ceil((double) leaderboardConfiguration.records / PAGE_SIZE);
        int page = 1;

        int startIndex = (page - 1) * PAGE_SIZE;
        List<Level> top = leaderboardService.getLeaderboard(startIndex, PAGE_SIZE);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(this.leaderboardConfiguration.embedSettings.title)
                .setColor(Color.decode(this.leaderboardConfiguration.embedSettings.color))
                .setFooter(String.format("Page %d/%d", page, totalPages), event.getUser().getAvatarUrl());

        if (top.isEmpty()) {
            embedBuilder.setDescription("The leaderboard is empty.");
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        StringBuilder leaderboardContent = new StringBuilder();
        int index = startIndex + 1;

        for (Level level : top) {
            int userLevel = level.getLevel();

            Formatter formatter = new Formatter()
                    .register("{index}", index)
                    .register("{user}", level.getId())
                    .register("{level}", userLevel);

            leaderboardContent.append(formatter.format("**{index}.** {user} - **LVL**: `{level}`")).append("\n");
            index++;
        }

        event.replyEmbeds(embedBuilder.setDescription(leaderboardContent.toString()).build())
                .addActionRow(
                        Button.primary("leaderboard_prev", "Previous").withEmoji(Emoji.fromFormatted("U+23EA")),
                        Button.primary("leaderboard_next", "Next").withEmoji(Emoji.fromUnicode("U+23E9"))
                )
                .queue();
    }

}
