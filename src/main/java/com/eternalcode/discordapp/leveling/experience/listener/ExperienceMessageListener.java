package com.eternalcode.discordapp.leveling.experience.listener;

import com.eternalcode.discordapp.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.leveling.experience.ExperienceService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

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

        try {
            this.givePoints(event);
        }
        catch (ExecutionException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void givePoints(MessageReceivedEvent event) throws ExecutionException, InterruptedException {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if (message.length < this.experienceConfig.messageExperience.howManyWords) {
            return;
        }

        double basePoints = this.experienceConfig.basePoints * this.experienceConfig.messageExperience.multiplier;
        double points = (double) message.length / this.experienceConfig.messageExperience.howManyWords * basePoints;
        long userId = event.getAuthor().getIdLong();

        this.experienceService.modifyPoints(userId, points, true).whenComplete((experience, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

}
