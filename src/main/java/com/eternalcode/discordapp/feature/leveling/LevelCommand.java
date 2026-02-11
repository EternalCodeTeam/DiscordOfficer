package com.eternalcode.discordapp.feature.leveling;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class LevelCommand extends SlashCommand {

    private final LevelService levelService;

    public LevelCommand(LevelService levelService) {
        this.name = "level";
        this.help = "Check your level or the level of another user";
        this.options = List.of(new OptionData(OptionType.USER, "user", "The user to check the level of")
                .setRequired(false)
        );
        this.levelService = levelService;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();

        this.levelService.find(user.getIdLong()).thenAccept(level -> {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Level")
                    .setDescription("Level of " + user.getAsMention())
                    .addField("Level", String.valueOf(level.getCurrentLevel()), true)
                    .setColor(0x00FF00);

            event.replyEmbeds(embedBuilder.build()).queue();
        });
    }
}
