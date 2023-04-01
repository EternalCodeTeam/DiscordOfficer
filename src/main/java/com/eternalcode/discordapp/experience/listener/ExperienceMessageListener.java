package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import com.eternalcode.discordapp.experience.ExperienceUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

public class ExperienceMessageListener extends ListenerAdapter {

    private final ExperienceRepository experienceRepository;
    private final ExperienceConfig experienceConfig;

    public ExperienceMessageListener(ExperienceRepository experienceRepository, ExperienceConfig experienceConfig) {
        this.experienceRepository = experienceRepository;
        this.experienceConfig = experienceConfig;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            this.givePoints(event);
        }
        catch (ExecutionException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void givePoints(MessageReceivedEvent event) throws ExecutionException, InterruptedException {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if (message.length < experienceConfig.messageExperience.howManyWords) {
            return;
        }

        double basePoints = experienceConfig.basePoints * experienceConfig.messageExperience.multiplier;
        double points = (double) message.length / experienceConfig.messageExperience.howManyWords * basePoints;

        ExperienceUtil.addPoints(this.experienceRepository, event.getAuthor().getIdLong(), points);
    }

}
