package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ExperienceReactionListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;
    private final ExperienceRepository experienceRepository;

    public ExperienceReactionListener(ExperienceConfig experienceConfig, ExperienceRepository experienceRepository) {
        this.experienceConfig = experienceConfig;
        this.experienceRepository = experienceRepository;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        long userId = event.getUserIdLong();
        double points = this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier;
        this.experienceRepository.modifyPoints(userId, points, true).whenComplete((status, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        long userId = event.getUserIdLong();
        double points = this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier;
        this.experienceRepository.modifyPoints(userId, points, false).whenComplete((status, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

}
