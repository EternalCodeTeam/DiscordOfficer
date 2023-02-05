package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class DiscordAppConfig implements CdnConfig {

    @Description("# The token of the bot")
    public String token = System.getenv("OFFICER_TOKEN") != null ? System.getenv("OFFICER_TOKEN") : "PASTE_TOKEN_HERE";

    @Description("# The ID of the owner of the bot")
    public long topOwnerId = System.getenv("OFFICER_OWNER") != null ? Long.parseLong(System.getenv("OFFICER_OWNER")) : 852920601969950760L;

    @Description("# The ID of guild")
    public long guildId = System.getenv("OFFICER_GUILD") != null ? Long.parseLong(System.getenv("OFFICER_GUILD")) : 1043190618526068767L;

    @Description("# The settings of embeds")
    public EmbedSettings embedSettings = new EmbedSettings();

    @Contextual
    public static class EmbedSettings {
        @Description("# Settings of the error embeds")
        public ErrorEmbed errorEmbed = new ErrorEmbed();

        @Description("# Settings of the success embeds")
        public SuccessEmbed successEmbed = new SuccessEmbed();

        @Contextual
        public static class ErrorEmbed {
            public String thumbnail = "https://i.imgur.com/2oTkWsr.png";
            public String color = "#e01947";
        }

        @Contextual
        public static class SuccessEmbed {
            public String thumbnail = "https://i.imgur.com/QkNxIL3.png";
            public String color = "#00ff77";
        }
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "config.yml");
    }
}
