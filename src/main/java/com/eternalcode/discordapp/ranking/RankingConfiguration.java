package com.eternalcode.discordapp.ranking;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class RankingConfiguration implements CdnConfig {
    @Description("# How many records should be displayed on the ranking")
    public int records = 10;

    @Description("# The settings of the embeds")
    public EmbedSettings embedSettings = new EmbedSettings();

    @Contextual
    public static class EmbedSettings {
        @Description("# Settings of the ranking embeds")

        @Description("# The color of the embed")
        public String color = "#e01947";

        @Description("# Footer of the embed")
        public String footer = "Ranking";

        @Description("# Thumbnail of the embed")
        public String thumbnail = "https://i.imgur.com/2oTkWsr.png";

        @Description("# The title of the embed")
        @Description("# {ranking} - the name of the ranking")
        @Description("# {records} - the number of records")
        public String title = "TOP{records} of {ranking}";
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "rankings.yml");
    }
}
