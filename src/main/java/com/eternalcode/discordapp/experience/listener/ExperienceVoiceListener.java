package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.data.UsersVoiceActivityData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;

public class ExperienceVoiceListener extends ListenerAdapter {

    private final ExperienceRepository experienceRepository;
    private final ExperienceConfig experienceConfig;

    private final UsersVoiceActivityData usersVoiceActivityData;
    private final ConfigManager dataManager;
    private final ExperienceService experienceService;

    public ExperienceVoiceListener(ExperienceRepository experienceRepository, ExperienceConfig experienceConfig, UsersVoiceActivityData usersVoiceActivityData, ConfigManager dataManager) {
        this.experienceRepository = experienceRepository;
        this.experienceConfig = experienceConfig;
        this.usersVoiceActivityData = usersVoiceActivityData;
        this.dataManager = dataManager;
        this.experienceService = new ExperienceService(this.experienceRepository);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        try {
            if (event.getMember().getUser().isBot()) {
                return;
            }

            this.leaveVoiceChannel(event);
            this.joinVoiceChannel(event);

            this.dataManager.save(this.usersVoiceActivityData);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void leaveVoiceChannel(GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != null) {
            this.usersVoiceActivityData.usersOnVoiceChannel.remove(event.getMember().getIdLong());
            this.experienceService.addPoints(event.getMember().getIdLong(), this.calculatePoints(event));
        }
    }

    private void joinVoiceChannel(GuildVoiceUpdateEvent event) {
        if (this.usersVoiceActivityData.usersOnVoiceChannel.containsKey(event.getMember().getIdLong())) {
            return;
        }

        if (event.getChannelJoined() != null) {
            this.usersVoiceActivityData.usersOnVoiceChannel.put(event.getMember().getIdLong(), Instant.now().toEpochMilli());
        }
    }

    private double calculatePoints(GuildVoiceUpdateEvent event) {
        long time = Instant.now().getEpochSecond() - this.usersVoiceActivityData.usersOnVoiceChannel.get(event.getMember().getIdLong());
        Instant instant = Instant.ofEpochSecond(time);
        LocalTime localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault());
        return (this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier) * localTime.getMinute() / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;
    }

}
