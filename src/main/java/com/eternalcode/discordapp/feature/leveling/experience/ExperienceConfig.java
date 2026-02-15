package com.eternalcode.discordapp.feature.leveling.experience;

import com.eternalcode.discordapp.config.CdnConfig;
import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class ExperienceConfig implements CdnConfig {

    @Description("# Base number of point that will be multiplied by the multiplier")
    public int basePoints = 3;

    @Description("# Message experience settings")
    public MessageExperience messageExperience = new MessageExperience();

    @Description("# Reaction experience settings")
    public ReactionExperience reactionExperience = new ReactionExperience();

    @Description("# Voice experience settings")
    public VoiceExperience voiceExperience = new VoiceExperience();


    @Contextual
    public static class MessageExperience {
        @Description("# The number of words to get experience")
        public int howManyWords = 3;

        @Description("# The multiplier of points that will be added to the user's experience")
        public double multiplier = 0.7;
    }

    @Contextual
    public static class ReactionExperience {
        @Description("# The multiplier of points that will be added to the user's experience")
        public double multiplier = 0.5;
    }

    @Contextual
    public static class VoiceExperience {
        @Description("# The number of minutes to get experience")
        public int howLongTimeSpendInVoiceChannel = 10;

        @Description("# The multiplier of points that will be added to the user's experience")
        public double multiplier = 1.2;
    }

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "experience.yml");
    }

}
