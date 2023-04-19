package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.data.UserOnVoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

public class ExperienceVoiceListener extends ListenerAdapter {

    private final ExperienceRepository experienceRepository;
    private final ExperienceConfig experienceConfig;

    private final UserOnVoiceChannel userOnVoiceChannel;
    private final ConfigManager dataManager;

    public ExperienceVoiceListener(ExperienceRepository experienceRepository, ExperienceConfig experienceConfig, UserOnVoiceChannel userOnVoiceChannel, ConfigManager dataManager) {
        this.experienceRepository = experienceRepository;
        this.experienceConfig = experienceConfig;
        this.userOnVoiceChannel = userOnVoiceChannel;
        this.dataManager = dataManager;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        try {
            if(event.getMember().getUser().isBot()) {
                return;
            }

            if(event.getChannelLeft() != null) {
                this.userOnVoiceChannel.removeUserOnVoiceChannel(event.getMember().getIdLong());
                long time = Instant.now().getEpochSecond() - this.userOnVoiceChannel.getUserTimeSpendOnVoiceChannel(event.getMember().getIdLong());
                Instant instant = Instant.ofEpochSecond(time);
                LocalTime localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault());

                double points = (this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier) * localTime.getMinute() / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;
                ExperienceService.addPoints(this.experienceRepository, event.getMember().getIdLong(), points);
            }

            if(event.getChannelJoined() != null) {
                this.userOnVoiceChannel.addUserOnVoiceChannel(event.getMember().getIdLong(), Instant.now());
            }

            this.dataManager.save(this.userOnVoiceChannel);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


}
