package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.leveling.experience.Experience;
import com.eternalcode.discordapp.leveling.experience.ExperienceChangeEvent;
import com.eternalcode.discordapp.observer.Observer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import panda.utilities.text.Formatter;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class LevelController implements Observer<ExperienceChangeEvent> {

    private final LevelConfig levelConfig;
    private final LevelService levelService;
    private final JDA jda;

    public LevelController(LevelConfig levelConfig, LevelService levelService, JDA jda) {
        this.levelConfig = levelConfig;
        this.levelService = levelService;
        this.jda = jda;
    }

    /**
     * This method is called when an ExperienceChangeEvent occurs. It updates the user's level based on their experience points
     * and sends a message to a designated TextChannel when the user levels up.
     * <p>
     * It calculates the next level using the formula: N = P * L^2 + P * L + P
     * where:
     * <p> - N represents the points needed to reach the next level,
     * <p> - P is the points needed for one level (provided by LevelConfig),
     * <p> - L is the current level.
     * <p>
     * Example level calculations:
     * <p> Level 1: N = 100 * 1^2 + 100 * 1 + 100 = 300
     * <p> Level 2: N = 100 * 2^2 + 100 * 2 + 100 = 700
     * <p> Level 3: N = 100 * 3^2 + 100 * 3 + 100 = 1200
     * <p> Level 4: N = 100 * 4^2 + 100 * 4 + 100 = 1700
     * <p> Level 5: N = 100 * 5^2 + 100 * 5 + 100 = 2300
     */

    @Override
    public void update(ExperienceChangeEvent event) {
        Experience experience = event.experience();
        long userId = experience.getUserId();

        double experiencePoints = experience.getPoints();

        double pointsNeededForOneLevel = this.levelConfig.points;

        this.levelService.find(userId).thenApply(userLevel -> {
            int currentLevel = userLevel.getCurrentLevel();

            int pointsNeededForNextLevel = (int) (pointsNeededForOneLevel * Math.pow(currentLevel + 1, 2)
                + pointsNeededForOneLevel * (currentLevel + 1)
                + pointsNeededForOneLevel);

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


            try {
                long channelId = event.channelId();

                MessageChannel channel = this.getChannelById(() -> channelId);

                if (channel == null) {
                    return null;
                }

                channel.sendMessage(messageContent).queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            }
            catch (Exception ignored) {

            }

            return userLevel;
        });
    }

    private MessageChannel getChannelById(LongSupplier channelId) {
        MessageChannel channel = this.jda.getPrivateChannelById(channelId.getAsLong());

        return channel != null ? channel : this.jda.getTextChannelById(channelId.getAsLong());
    }
}
