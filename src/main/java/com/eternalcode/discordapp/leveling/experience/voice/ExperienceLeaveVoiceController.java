package com.eternalcode.discordapp.leveling.experience.voice;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.leveling.experience.ExperienceService;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Duration;
import java.time.Instant;
import java.util.function.LongSupplier;

public class ExperienceLeaveVoiceController extends ListenerAdapter {

    private final ExperienceVoiceActivityData experienceVoiceActivityData;
    private final ConfigManager configManager;
    private final ExperienceConfig experienceConfig;
    private final ExperienceService experienceService;

    public ExperienceLeaveVoiceController(ExperienceVoiceActivityData experienceVoiceActivityData, ConfigManager configManager,
                                          ExperienceConfig experienceConfig, ExperienceService experienceService) {
        this.experienceVoiceActivityData = experienceVoiceActivityData;
        this.configManager = configManager;
        this.experienceConfig = experienceConfig;
        this.experienceService = experienceService;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
            return;
        }

        long userId = event.getMember().getIdLong();

        LongSupplier voiceLeftChannelId = () -> event.getChannelLeft().getIdLong();
        this.experienceService.modifyPoints(userId, this.calculatePoints(event), true, voiceLeftChannelId).whenComplete((experience, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });

        this.experienceVoiceActivityData.usersOnVoiceChannel.remove(event.getMember().getIdLong());
        this.configManager.save(this.experienceVoiceActivityData);
    }


    private double calculatePoints(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();

        Instant joined = this.experienceVoiceActivityData.usersOnVoiceChannel.get(userId);
        long minutes = Duration.between(joined, Instant.now()).toMinutes();

        double basePoints = this.experienceConfig.basePoints * this.experienceConfig.voiceExperience.multiplier;
        return basePoints * minutes / this.experienceConfig.voiceExperience.howLongTimeSpendInVoiceChannel;
    }

}
