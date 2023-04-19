package com.eternalcode.discordapp.experience.data;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;

public class UserOnVoiceChannel implements CdnConfig {

    @Description({
            "# The map of users on voice channels",
            "# Key - user ID",
            "# Value - time in seconds",
            "# It's automatically generated, don't touch it!"
    })
    public HashMap<Long, Long> usersOnVoiceChannel = new HashMap<>();

    public long getUserTimeSpendOnVoiceChannel(long userId) {
        return this.usersOnVoiceChannel.getOrDefault(userId, 0L);
    }

    public void addUserOnVoiceChannel(long userId, Instant instant) {
        if(this.usersOnVoiceChannel.containsKey(userId)) {
            this.usersOnVoiceChannel.replace(userId, instant.getEpochSecond());
        } else {
            this.usersOnVoiceChannel.put(userId, instant.getEpochSecond());
        }
    }

    public void removeUserOnVoiceChannel(long userId) {
        this.usersOnVoiceChannel.remove(userId);
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "userOnVoiceChannel.yml");
    }
}
