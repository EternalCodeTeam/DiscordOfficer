package com.eternalcode.discordapp.leveling.experience.voice;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ExperienceVoiceActivityData implements CdnConfig {

    public Map<Long, Instant> usersOnVoiceChannel = new HashMap<>();

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "userOnVoiceChannel.dat");
    }
}
