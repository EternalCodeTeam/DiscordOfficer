package com.eternalcode.discordapp.leveling.experience.voice;

import com.eternalcode.discordapp.config.ConfigManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public class ExperienceJoinVoiceController extends ListenerAdapter {

    private final ExperienceVoiceActivityData experienceVoiceActivityData;
    private final ConfigManager configManager;

    public ExperienceJoinVoiceController(ExperienceVoiceActivityData experienceVoiceActivityData, ConfigManager configManager) {
        this.experienceVoiceActivityData = experienceVoiceActivityData;
        this.configManager = configManager;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        long userId = event.getMember().getIdLong();

        if (this.experienceVoiceActivityData.usersOnVoiceChannel.containsKey(userId)) {
            return;
        }

        if (event.getChannelJoined() == null) {
            return;
        }

        this.experienceVoiceActivityData.usersOnVoiceChannel.put(userId, Instant.now());
        this.configManager.save(this.experienceVoiceActivityData);
    }

}
