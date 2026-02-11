package com.eternalcode.discordapp.feature.leveling;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class LevelConfig implements CdnConfig {

    @Description("# The count of points that will be added to the user's level")
    public int points = 100;

    public Message message = new Message();

    @Contextual
    public static class Message {
        @Description({ "# Description of embed", "# {user} - mention user", "# {level} - level" })
        public String description = "{user} has reached level {level}!";
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "level.yml");
    }
}
