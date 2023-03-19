package com.eternalcode.discordapp.expierience;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ExecutionException;

public class ExpierienceListener extends ListenerAdapter {
    private final ExperienceRepository experienceRepository;
    private static final int HOW_MANY_WORDS_TO_GIVE_POINTS = 5;

    public ExpierienceListener(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
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

        if (message.length < HOW_MANY_WORDS_TO_GIVE_POINTS) {
            return;
        }

        int points = message.length / HOW_MANY_WORDS_TO_GIVE_POINTS * 10;

        this.experienceRepository.findUser(event.getAuthor().getIdLong()).whenComplete((experience, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            experience.addPoints(points);

            this.experienceRepository.saveUser(experience).whenComplete((createOrUpdateStatus, throwable1) -> {
                if (throwable1 != null) {
                    throwable1.printStackTrace();
                }
            });
        });
    }

}
