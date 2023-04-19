package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.data.DataManager;
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.data.UsersVoiceActivityData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public class ExperienceVoiceListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;

    private final UsersVoiceActivityData usersVoiceActivityData;
    private final DataManager dataManager;
    private final ExperienceService experienceService;

    public ExperienceVoiceListener(ExperienceConfig experienceConfig, UsersVoiceActivityData usersVoiceActivityData, DataManager dataManager, ExperienceService experienceService) {
        this.experienceConfig = experienceConfig;
        this.usersVoiceActivityData = usersVoiceActivityData;
        this.dataManager = dataManager;
        this.experienceService = experienceService;
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
            long userId = event.getMember().getIdLong();
            this.usersVoiceActivityData.usersOnVoiceChannel.remove(event.getMember().getIdLong());
            this.experienceService.addPoints(userId, this.calculatePoints(event));
        }
    }

    private void joinVoiceChannel(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();
        if (this.usersVoiceActivityData.usersOnVoiceChannel.containsKey(userId)) {
            return;
        }

        if (event.getChannelJoined() != null) {
            this.usersVoiceActivityData.usersOnVoiceChannel.put(userId, Instant.now().toEpochMilli());
        }
    }

    private double calculatePoints(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();
        
        long timeSpentOnChannel = Instant.now().getEpochSecond() - this.usersVoiceActivityData.usersOnVoiceChannel.get(userId);
        Instant instant = Instant.ofEpochSecond(timeSpentOnChannel);
        LocalTime localTime = LocalTime.ofInstant(instant, ZoneId.systemDefault());
        
        double points = (this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier) * localTime.getMinute() / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;
        
        return points;
    }

}
