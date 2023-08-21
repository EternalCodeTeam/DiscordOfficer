package com.eternalcode.discordapp.guildstats;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import panda.utilities.text.Formatter;

import java.util.Map;

public class GuildStatisticsService {

    private final AppConfig config;
    private final JDA jda;

    public GuildStatisticsService(AppConfig config, JDA jda) {
        this.config = config;
        this.jda = jda;
    }

    public void displayStats() {
        Guild guild = this.jda.getGuildById(this.config.guildId);
        if (guild == null) {
            return;
        }

        for (Map.Entry<Long, String> entry : this.config.voiceChannelStatistics.channelNames.entrySet()) {
            Long key = entry.getKey();
            String value = entry.getValue();

            VoiceChannel channel = guild.getVoiceChannelById(key);

            if (channel == null) {
                continue;
            }

            Formatter formatter = new Formatter()
                    .register("{MEMBERS_SIZE}", guild.getMemberCache().stream()
                            .filter(member -> !member.getUser().isBot())
                            .count()
                    )

                    .register("{ONLINE_MEMBERS_SIZE}", guild.getMemberCache().stream()
                            .filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE)
                            .filter(member -> !member.getUser().isBot())
                            .count()
                    )

                    .register("{BOT_MEMBERS_SIZE}", guild.getMembers().stream()
                            .filter(member -> member.getUser().isBot())
                            .count()
                    )

                    .register("{CHANNELS_SIZE}", guild.getChannels().size())
                    .register("{ROLES_SIZE}", guild.getRoles().size())
                    .register("{TEXT_CHANNELS_SIZE}", guild.getTextChannels().size())
                    .register("{VOICE_CHANNELS_SIZE}", guild.getVoiceChannels().size())
                    .register("{CATEGORIES_SIZE}", guild.getCategories().size())
                    .register("{EMOJIS_SIZE}", guild.getEmojis().size())
                    .register("{BOOSTS_SIZE}", guild.getBoostCount())
                    .register("{BOOST_TIER}", guild.getBoostTier().getKey());


            String stats = formatter.format(value);
            channel.getManager().setName(stats).queue();
        }
    }
}
