package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.leveling.Level;
import com.eternalcode.discordapp.leveling.LevelService;
import com.eternalcode.discordapp.ranking.RankingConfiguration;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import panda.utilities.text.Formatter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardCommand extends SlashCommand {

    private final LevelService levelService;
    private final RankingConfiguration rankingConfiguration;

    public LeaderboardCommand(LevelService levelService, RankingConfiguration rankingConfiguration) {
        this.levelService = levelService;
        this.rankingConfiguration = rankingConfiguration;

        this.name = "level";
        this.help = String.format("Shows the top %s users in level ranking", this.rankingConfiguration.records);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int page = 1;
        int pageSize = 10;
        int totalPages = (int) Math.ceil(this.rankingConfiguration.records / (double) pageSize);

        LeaderboardService leaderboardService = new LeaderboardService(this.levelService);
        List<Level> top = leaderboardService.getLeaderboard((page - 1) * pageSize + 1, page * pageSize);

        String title = new Formatter()
            .register("{ranking}", this.name)
            .register("{records}", this.rankingConfiguration.records)
            .format(this.rankingConfiguration.embedSettings.title);

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle(title)
            .setColor(Color.decode(this.rankingConfiguration.embedSettings.color))
            .setFooter(this.rankingConfiguration.embedSettings.footer, event.getUser().getAvatarUrl());

        int index = (page - 1) * pageSize + 1;
        for (Level level : top) {
            Formatter formatter = new Formatter()
                .register("{index}", index)
                .register("{user}", event.getGuild().getMemberById(level.getId()).getEffectiveName())
                .register("{level}", level.getLevel());


            embedBuilder.addField(
                formatter.format("#{index} - {user}"),
                formatter.format("Level: {level}"),
                false
            );

            index++;
        }

        event.replyEmbeds(embedBuilder.build())
            .addActionRow(
                Button.primary("leaderboard_next", "Next"),
                Button.primary("leaderboard_prev", "Previous")
            )
            .queue();
    }
}
