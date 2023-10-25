package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.leveling.experience.Experience;
import com.eternalcode.discordapp.leveling.experience.ExperienceChangeEvent;
import com.eternalcode.discordapp.observer.Observer;
import com.eternalcode.discordapp.util.LevelUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import panda.utilities.text.Formatter;

public class LevelController implements Observer<ExperienceChangeEvent> {

    private final LevelConfig levelConfig;
    private final LevelService levelService;
    private final JDA jda;

    public LevelController(LevelConfig levelConfig, LevelService levelService, JDA jda) {
        this.levelConfig = levelConfig;
        this.levelService = levelService;
        this.jda = jda;
    }

    @Override
    public void update(ExperienceChangeEvent experienceChangeEvent) {
        Experience experience = experienceChangeEvent.experience();
        long userId = experience.getUserId();

        double experiencePoints = experience.getPoints();

        double pointsNeededForOneLevel = this.levelConfig.points;

        this.levelService.find(userId).thenApply(userLevel -> {
            int currentLevel = userLevel.getCurrentLevel();

            int pointsNeededForNextLevel = LevelUtil.calculatePointsForNextLevel(currentLevel, pointsNeededForOneLevel);

            if (experiencePoints <= pointsNeededForNextLevel) {
                return null;
            }

            int newLevel = currentLevel + 1;

            userLevel.setCurrentLevel(newLevel);
            this.levelService.saveLevel(userLevel);

            User user = this.jda.getUserById(userId);
            if (user == null) {
                return null;
            }

            String messageContent = new Formatter()
                .register("{user}", user.getAsMention())
                .register("{level}", String.valueOf(newLevel))
                .format(this.levelConfig.message.description);

            TextChannel levelChannel = this.jda.getTextChannelById(this.levelConfig.channel);
            if (levelChannel != null) {
                levelChannel.sendMessage(messageContent).queue();
            }

            return userLevel;
        });
    }
}
