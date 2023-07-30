package com.eternalcode.discordapp.ranking;

import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.command.Top10ExperienceCommand;
import com.eternalcode.discordapp.leveling.LevelService;
import com.eternalcode.discordapp.leveling.command.Top10LevelCommand;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class TopCommand extends SlashCommand {

    public TopCommand(LevelService levelService, ExperienceService experienceService) {
        this.name = "top";
        this.help = "Shows the top 10 users with selected ranking type";

        this.children = new SlashCommand[] {
                new Top10LevelCommand(levelService),
                new Top10ExperienceCommand(experienceService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
    }
}
