package com.eternalcode.discordapp.feature.guildstats;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.utilities.text.Formatter;

public final class GuildStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuildStatisticsService.class);

    private final AppConfig config;
    private final JDA jda;

    public GuildStatisticsService(AppConfig config, JDA jda) {
        this.config = config;
        this.jda = jda;
    }

    public CompletableFuture<Void> displayStats() {
        return CompletableFuture.runAsync(() -> {
            Guild guild = jda.getGuildById(config.guildId);
            if (guild == null) {
                LOGGER.warn("Guild not found with ID: {}", config.guildId);
                return;
            }

            LOGGER.info("Updating guild statistics for guild: {}", guild.getName());

            for (Map.Entry<Long, String> entry : config.voiceChannelStatistics.channelNames.entrySet()) {
                Long channelId = entry.getKey();
                String nameTemplate = entry.getValue();

                this.updateChannelStatistics(guild, channelId, nameTemplate);
            }

            LOGGER.info("Guild statistics update completed");
        }).exceptionally(FutureHandler::handleException);
    }

    private void updateChannelStatistics(Guild guild, Long channelId, String nameTemplate) {
        VoiceChannel channel = guild.getVoiceChannelById(channelId);

        if (channel == null) {
            LOGGER.warn("Voice channel not found with ID: {}", channelId);
            return;
        }

        try {
            String formattedName = formatChannelName(guild, nameTemplate);

            channel.getManager()
                .setName(formattedName)
                .queue(
                    success -> LOGGER.debug("Updated channel '{}' statistics", channel.getName()),
                    error -> LOGGER.error("Failed to update channel '{}': {}", channel.getName(), error.getMessage())
                );
        }
        catch (Exception exception) {
            LOGGER.error("Error formatting statistics for channel '{}': {}", channel.getName(), exception.getMessage());
        }
    }

    private String formatChannelName(Guild guild, String nameTemplate) {
        long membersCount = guild.getMemberCache().stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        long onlineMembersCount = guild.getMemberCache().stream()
            .filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE)
            .filter(member -> !member.getUser().isBot())
            .count();

        long botMembersCount = guild.getMembers().stream()
            .filter(member -> member.getUser().isBot())
            .count();

        Formatter formatter = new Formatter()
            .register("{MEMBERS_SIZE}", membersCount)
            .register("{ONLINE_MEMBERS_SIZE}", onlineMembersCount)
            .register("{BOT_MEMBERS_SIZE}", botMembersCount)
            .register("{CHANNELS_SIZE}", guild.getChannels().size())
            .register("{ROLES_SIZE}", guild.getRoles().size())
            .register("{TEXT_CHANNELS_SIZE}", guild.getTextChannels().size())
            .register("{VOICE_CHANNELS_SIZE}", guild.getVoiceChannels().size())
            .register("{CATEGORIES_SIZE}", guild.getCategories().size())
            .register("{EMOJIS_SIZE}", guild.getEmojis().size())
            .register("{BOOSTS_SIZE}", guild.getBoostCount())
            .register("{BOOST_TIER}", guild.getBoostTier().getKey());

        return formatter.format(nameTemplate);
    }
}
