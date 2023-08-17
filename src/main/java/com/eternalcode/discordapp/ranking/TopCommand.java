package com.eternalcode.discordapp.ranking;

import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.command.Top10ExperienceCommand;
import com.eternalcode.discordapp.leveling.LevelService;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardCommand;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class TopCommand extends SlashCommand {

    public TopCommand(LevelService levelService, ExperienceService experienceService, RankingConfiguration rankingConfiguration) {
        this.name = "top";
        this.help = String.format("Shows the top %s users with selected ranking type", rankingConfiguration.records);

        this.children = new SlashCommand[] {
                new LeaderboardCommand(levelService, rankingConfiguration),
                new Top10ExperienceCommand(experienceService, rankingConfiguration)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }
}
