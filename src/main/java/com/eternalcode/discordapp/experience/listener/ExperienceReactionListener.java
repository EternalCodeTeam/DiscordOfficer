package com.eternalcode.discordapp.experience.listener;

import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
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
        // TODO: Add experience
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        // TODO: Remove experience
    }
}
