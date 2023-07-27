package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.experience.data.UsersVoiceActivityData;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Duration;
import java.time.Instant;

public class ExperienceVoiceListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;

    private final UsersVoiceActivityData usersVoiceActivityData;
    private final ConfigManager configManager;
    private final ExperienceRepository experienceRepository;

    public ExperienceVoiceListener(ExperienceConfig experienceConfig, UsersVoiceActivityData usersVoiceActivityData, ConfigManager configManager, ExperienceRepository experienceRepository) {
        this.experienceConfig = experienceConfig;
        this.usersVoiceActivityData = usersVoiceActivityData;
        this.configManager = configManager;
        this.experienceRepository = experienceRepository;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        try {
            if (event.getMember().getUser().isBot()) {
                return;
            }

            this.leaveVoiceChannel(event);
            this.joinVoiceChannel(event);

            this.configManager.save(this.usersVoiceActivityData);
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
        this.experienceService.modifyPoints(userId, this.calculatePoints(event), true).whenComplete((experience, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
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

        Instant joined = this.usersVoiceActivityData.usersOnVoiceChannel.get(userId);
        long minutes = Duration.between(joined, Instant.now()).toMinutes();

        double basePoints = this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier;
        double points = basePoints * minutes / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;

        return points;
    }

}
