package com.eternalcode.discordapp.leveling.experience.data;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;

public class UsersVoiceActivityData implements CdnConfig {

    public HashMap<Long, Instant> usersOnVoiceChannel = new HashMap<>();

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "userOnVoiceChannel.dat");
    }
}
