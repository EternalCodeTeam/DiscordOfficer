package com.eternalcode.discordapp.feature.leveling.experience.listener;

import com.eternalcode.discordapp.feature.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.LongSupplier;

public class ExperienceMessageListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;
    private final ExperienceService experienceService;

    public ExperienceMessageListener(ExperienceConfig experienceConfig, ExperienceService experienceService) {
        this.experienceConfig = experienceConfig;
        this.experienceService = experienceService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }

        this.givePoints(event);
    }

    private void givePoints(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if (message.length < this.experienceConfig.messageExperience.howManyWords) {
            return;
        }

        double basePoints = this.experienceConfig.basePoints * this.experienceConfig.messageExperience.multiplier;
        double points = (double) message.length / this.experienceConfig.messageExperience.howManyWords * basePoints;
        long userId = event.getAuthor().getIdLong();

        LongSupplier channelId = () -> event.getChannel().getIdLong();
        this.experienceService.modifyPoints(userId, points, true, channelId).whenComplete((experience, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

}
