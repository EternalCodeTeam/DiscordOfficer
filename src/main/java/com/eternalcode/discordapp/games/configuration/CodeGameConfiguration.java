package com.eternalcode.discordapp.games.configuration;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class CodeGameConfiguration implements CdnConfig {
    @Description("# Channel id where the game will be played")
    public Long channelId = 0L;

    @Description("# Maximum points to win - default: 10")
    public int maxPoints = 10;

    @Description("# The time in minutes to generate new code - default: 5")
    public int timeToNextQuestion = 5;


    @Description("# The text in message with code")
    public String codeText = "The code is: ";

    @Description("# Winner message settings")
    public EmbedSettings embedSettings = new EmbedSettings();

    @Contextual
    public static class EmbedSettings {
        @Description("# Settings of the win embed")
        @Description("# Title of the embed")
        public String title = "Winner!";

        @Description("# Description of the embed")
        @Description("# Placeholders: ")
        @Description("# {winner} - The winner of the game")
        @Description("# {points} - The points of the winner")
        @Description("# {time} - The time of the game")
        public String description = "The winner is: {winner}\n" +
                "Points: {points}\n" +
                "Time: {time} minutes";

        @Description("# Color of the embed")
        public String color = "#00ff00";

        @Description("# Footer of the embed")
        public String footer = "Code game";
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "games/code.yml");
    }
}
