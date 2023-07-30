package com.eternalcode.discordapp.experience.command;

import com.eternalcode.discordapp.experience.Experience;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.leveling.Level;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import panda.utilities.text.Formatter;

import java.util.List;

public class Top10ExperienceCommand extends SlashCommand {

    private ExperienceService experienceService;

    public Top10ExperienceCommand(ExperienceService experienceService) {
        this.name = "experience";
        this.help = "Shows the top 10 users in experience ranking";

        this.experienceService = experienceService;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Experience> top = this.experienceService.getTop(10, 0).join();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Top 10 users in experience ranking")
                .setColor(0x00FF00)
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl());

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
