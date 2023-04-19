package com.eternalcode.discordapp.experience.data;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;

public class UsersVoiceActivityData implements CdnConfig {

    @Description({
            "# The map of users on voice channels",
            "# Key - user ID",
            "# Value - time in seconds",
            "# It's automatically generated, don't touch it!"
    })
    public HashMap<Long, Long> usersOnVoiceChannel = new HashMap<>();

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "userOnVoiceChannel.yml");
    }
}
