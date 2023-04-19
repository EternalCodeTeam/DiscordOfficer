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

    public ExperienceReactionListener(ExperienceRepository experienceRepository, ExperienceConfig experienceConfig) {
        this.experienceRepository = experienceRepository;
        this.experienceConfig = experienceConfig;
    }


    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        ExperienceService.addPoints(this.experienceRepository, event.getUserIdLong(), this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        ExperienceService.removePoints(this.experienceRepository, event.getUserIdLong(), this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier);
    }
}
