package com.eternalcode.discordapp.leveling.command;

import com.eternalcode.discordapp.leveling.Level;
import com.eternalcode.discordapp.leveling.LevelService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import panda.utilities.text.Formatter;

import java.util.List;

public class Top10LevelCommand extends SlashCommand {
    private final LevelService levelService;

    public Top10LevelCommand(LevelService levelService) {
        this.levelService = levelService;

        this.name = "level";
        this.help = "Shows the top 10 users in level ranking";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Level> top = this.levelService.getTop(10, 0).join();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Top 10 users in level ranking")
                .setColor(0x00FF00)
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl());

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
