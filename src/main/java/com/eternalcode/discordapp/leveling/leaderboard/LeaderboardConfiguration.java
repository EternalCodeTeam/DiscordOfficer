package com.eternalcode.discordapp.leveling.leaderboard;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class LeaderboardConfiguration implements CdnConfig {

    @Description("# How many records should be displayed on the ranking")
    public int records = 100;

    @Description("# The settings of the embeds")
    public EmbedSettings embedSettings = new EmbedSettings();

    @Contextual
    public static class EmbedSettings {
        @Description("# Settings of the ranking embeds")

        @Description("# The color of the embed")
        public String color = "#e01947";

        @Description("# The title of the embed")
        public String title = "Leaderboard";
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "rankings.yml");
    }
}
