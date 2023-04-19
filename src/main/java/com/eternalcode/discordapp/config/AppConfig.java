package com.eternalcode.discordapp.config;

import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AppConfig implements CdnConfig {

    @Description("# The token of the bot")
    public String token = System.getenv("OFFICER_TOKEN") != null ? System.getenv("OFFICER_TOKEN") : "PASTE_TOKEN_HERE";

    @Description("# The ID of the owner of the bot")
    public long topOwnerId = System.getenv("OFFICER_OWNER") != null ? Long.parseLong(System.getenv("OFFICER_OWNER")) : 852920601969950760L;

    @Description("# The ID of guild")
    public long guildId = System.getenv("OFFICER_GUILD") != null ? Long.parseLong(System.getenv("OFFICER_GUILD")) : 1043190618526068767L;

    @Description("# The app discord token")
    public String githubToken = System.getenv("OFFICER_GITHUB_TOKEN") != null ? System.getenv("OFFICER_GITHUB_TOKEN") : "PASTE_GITHUB_TOKEN_HERE";

    @Description("# Sentry DSN")
    public String sentryDsn = System.getenv("OFFICER_SENTRY_DSN") != null ? System.getenv("OFFICER_SENTRY_DSN") : "PASTE_SENTRY_DSN_HERE";

    @Description("# The settings of embeds")
    public EmbedSettings embedSettings = new EmbedSettings();

    @Description("# The settings of voice channel statistics")
    public VoiceChannelStatistics voiceChannelStatistics = new VoiceChannelStatistics();

    @Description("# The settings of review system")
    public ReviewSystem reviewSystem = new ReviewSystem();

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "config.yml");
    }

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

    @Contextual
    public static class VoiceChannelStatistics {
        @Description({
                "# Placeholders:",
                "# {MEMBERS_SIZE} - the number of members",
                "# {ONLINE_MEMBERS_SIZE} - the number of online members",
                "# {BOT_MEMBERS_SIZE} - the number of bot members",
                "# {CHANNELS_SIZE} - the number of channels",
                "# {ROLES_SIZE} - the number of roles",
                "# {TEXT_CHANNELS_SIZE} - the number of text channels",
                "# {VOICE_CHANNELS_SIZE} - the number of voice channels",
                "# {CATEGORIES_SIZE} - the number of categories",
                "# {EMOJIS_SIZE} - the number of emojis",
                "# {BOOSTS_SIZE} - the number of boosts",
                "# {BOOST_TIER} - the boost tier",
        })
        public Map<Long, String> channelNames = new HashMap<>(Map.of(
                1043190619729842217L, "Members: {MEMBERS_SIZE}",
                1088950113302495403L, "Channels: {CHANNELS_SIZE}",
                1088950123192660068L, "Roles: {ROLES_SIZE}",
                1088950131660963932L, "Text Channels: {TEXT_CHANNELS_SIZE}",
                1088951302257647717L, "Voice Channels: {VOICE_CHANNELS_SIZE}",
                1088951310839206088L, "Emojis: {EMOJIS_SIZE}",
                1088951320033112095L, "Boosts: {BOOSTS_SIZE}",
                1088951327964528723L, "Boost Tier: {BOOST_TIER}",
                1088951511662460988L, "Online Users: {ONLINE_MEMBERS_SIZE}"
        ));
    }

    @Contextual
    public static class ReviewSystem {
        public long reviewForumId = 1090383282744590396L;

        public Map<String, Long> reviewers = new HashMap<>(Map.of(
                "vLuckyyy", 852920601969950760L,
                "Embrejs", 887998351239413821L,
                "eripe14", 406091568378937344L,
                "Hyd3r1", 1038402657594908712L,
                "Jakubk15", 533345209434767372L,
                "Kamicjusz", 477473823546015744L,
                "Osnixer", 719653565026664628L,
                "Rollczi", 534018824559788032L
        ));
    }
}
