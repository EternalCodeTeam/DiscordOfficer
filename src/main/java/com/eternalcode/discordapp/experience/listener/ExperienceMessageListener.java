package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

public class ExperienceMessageListener extends ListenerAdapter {

    private final ExperienceConfig experienceConfig;
    private final ExperienceRepository experienceRepository;

    public ExperienceMessageListener(ExperienceConfig experienceConfig, ExperienceRepository experienceRepository) {
        this.experienceConfig = experienceConfig;
        this.experienceRepository = experienceRepository;
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

        this.experienceRepository.modifyPoints(userId, points, true).whenComplete((status, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

}
