package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import com.eternalcode.discordapp.experience.ExperienceService;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ExperienceReactionListener extends ListenerAdapter {

    private final ExperienceRepository experienceRepository;
    private final ExperienceConfig experienceConfig;
    private final ExperienceService experienceService;

    public ExperienceReactionListener(ExperienceRepository experienceRepository, ExperienceConfig experienceConfig) {
        this.experienceRepository = experienceRepository;
        this.experienceConfig = experienceConfig;
        this.experienceService = new ExperienceService(this.experienceRepository);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        this.experienceService.addPoints(event.getUserIdLong(), this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        this.experienceService.removePoints(event.getUserIdLong(), this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier);
    }

}
