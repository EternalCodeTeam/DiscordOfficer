package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceService;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ExperienceReactionListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;
    private final ExperienceService experienceService;

    public ExperienceReactionListener(ExperienceConfig experienceConfig, ExperienceService experienceService) {
        this.experienceConfig = experienceConfig;
        this.experienceService = experienceService;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        long userId = event.getUserIdLong();
        double points = this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier;
        this.experienceService.addPoints(userId, points);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        long userId = event.getUserIdLong();
        double points = this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier;
        this.experienceService.removePoints(userId, points);
    }

}
