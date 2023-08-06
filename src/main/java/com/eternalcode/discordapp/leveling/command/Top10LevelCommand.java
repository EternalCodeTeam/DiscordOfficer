package com.eternalcode.discordapp.leveling.command;

import com.eternalcode.discordapp.leveling.Level;
import com.eternalcode.discordapp.leveling.LevelService;
import com.eternalcode.discordapp.ranking.RankingConfiguration;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import panda.utilities.text.Formatter;

import java.awt.Color;
import java.util.List;

public class Top10LevelCommand extends SlashCommand {
    private final LevelService levelService;
    private final RankingConfiguration rankingConfiguration;

    public Top10LevelCommand(LevelService levelService, RankingConfiguration rankingConfiguration) {
        this.levelService = levelService;
        this.rankingConfiguration = rankingConfiguration;

        this.name = "level";
        this.help = String.format("Shows the top %s users in level ranking", this.rankingConfiguration.records);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Level> top = this.levelService.getTop(this.rankingConfiguration.records, 0).join();

        String title = new Formatter()
                .register("{ranking}", this.name)
                .register("{records}", this.rankingConfiguration.records)
                .format(this.rankingConfiguration.embedSettings.title);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.decode(this.rankingConfiguration.embedSettings.color))
                .setFooter(this.rankingConfiguration.embedSettings.footer, event.getUser().getAvatarUrl());

        int index = 1;
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

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
