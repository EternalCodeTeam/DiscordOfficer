package com.eternalcode.discordapp.feature.leveling.experience.listener;

import com.eternalcode.discordapp.feature.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.LongSupplier;

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

        User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.modifyPoints(user, userId, points);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        long userId = event.getUserIdLong();
        double points = this.experienceConfig.basePoints * this.experienceConfig.reactionExperience.multiplier;

        User user = event.getUser();

        if (user.isBot()) {
            return;
        }

        this.modifyPoints(user, userId, points);
    }

    private void modifyPoints(User event, long userId, double points) {
        LongSupplier channel = () -> event.openPrivateChannel().complete().getIdLong();

        this.experienceService.modifyPoints(userId, points, true, channel)
            .whenComplete((experience, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            });
    }

}

