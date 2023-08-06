package com.eternalcode.discordapp.experience.command;

import com.eternalcode.discordapp.experience.Experience;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.ranking.RankingConfiguration;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import panda.utilities.text.Formatter;

import java.awt.Color;
import java.util.List;

public class Top10ExperienceCommand extends SlashCommand {

    private final ExperienceService experienceService;
    private final RankingConfiguration rankingConfiguration;

    public Top10ExperienceCommand(ExperienceService experienceService, RankingConfiguration rankingConfiguration) {
        this.experienceService = experienceService;
        this.rankingConfiguration = rankingConfiguration;

        this.name = "experience";
        this.help = String.format("Shows the top %s users in experience ranking", this.rankingConfiguration.records);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Experience> top = this.experienceService.getTop(this.rankingConfiguration.records, 0).join();

        String title = new Formatter()
                .register("{ranking}", this.name)
                .register("{records}", this.rankingConfiguration.records)
                .format(this.rankingConfiguration.embedSettings.title);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.decode(this.rankingConfiguration.embedSettings.color))
                .setFooter(this.rankingConfiguration.embedSettings.footer, event.getUser().getAvatarUrl());

        int index = 1;
        for (Experience experience : top) {
            Formatter formatter = new Formatter()
                    .register("{index}", index)
                    .register("{user}", event.getGuild().getMemberById(experience.getUserId()).getEffectiveName())
                    .register("{xp}", Math.round(experience.getPoints()));


            embedBuilder.addField(
                    formatter.format("#{index} - {user}"),
                    formatter.format("XP: {xp}"),
                    false
            );

            index++;
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
