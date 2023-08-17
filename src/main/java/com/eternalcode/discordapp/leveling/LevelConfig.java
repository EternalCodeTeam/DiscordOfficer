package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class LevelConfig implements CdnConfig {

    @Description("# The count of points that will be added to the user's level")
    public int points = 100;

    @Description("# Channel where the message will be sent")
    public long channel = 0;

    public Message message = new Message();

    @Contextual
    public static class Message {
        @Description("# Color of embed")
        public String color = "#00ff00";

        @Description("# Title of embed")
        public String title = "Level up!";

        @Description({"# Description of embed", "# {user} - mention user", "# {level} - level"})
        public String description = "{user} has reached level {level}!";

        @Description("# Thumbnail of embed")
        public String thumbnail = "https://cdn.discordapp.com/attachments/773784000000000000/773784033000000000/level.png";

        @Description("# Footer of embed")
        public String footer = "Leveling system";
    }


    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "level.yml");
    }
}
