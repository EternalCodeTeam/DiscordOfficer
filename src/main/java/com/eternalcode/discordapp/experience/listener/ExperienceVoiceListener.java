package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.data.YamlFilesManager;
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.data.UsersVoiceActivityData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public class ExperienceVoiceListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;

    private final UsersVoiceActivityData usersVoiceActivityData;
    private final YamlFilesManager yamlFilesManager;
    private final ExperienceService experienceService;

    public ExperienceVoiceListener(ExperienceConfig experienceConfig, UsersVoiceActivityData usersVoiceActivityData, YamlFilesManager yamlFilesManager, ExperienceService experienceService) {
        this.experienceConfig = experienceConfig;
        this.usersVoiceActivityData = usersVoiceActivityData;
        this.yamlFilesManager = yamlFilesManager;
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

            this.yamlFilesManager.save(this.usersVoiceActivityData);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void leaveVoiceChannel(GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
           return;
        }

        long userId = event.getMember().getIdLong();
        this.usersVoiceActivityData.usersOnVoiceChannel.remove(event.getMember().getIdLong());
        this.experienceService.addPoints(userId, this.calculatePoints(event));
    }

    private void joinVoiceChannel(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();
        if (this.usersVoiceActivityData.usersOnVoiceChannel.containsKey(userId)) {
            return;
        }

        if (event.getChannelJoined() == null) {
            return;
        }

        this.usersVoiceActivityData.usersOnVoiceChannel.put(userId, Instant.now());
    }

    private double calculatePoints(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();
        
        long minutes = Duration.between(this.usersVoiceActivityData.usersOnVoiceChannel.get(userId), Instant.now()).toMinutes();
        
        double points = (this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier) * minutes / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;
        
        return points;
    }

}
