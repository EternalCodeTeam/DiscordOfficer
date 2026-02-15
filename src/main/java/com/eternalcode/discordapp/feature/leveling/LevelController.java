package com.eternalcode.discordapp.feature.leveling;

import com.eternalcode.discordapp.feature.leveling.experience.Experience;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceChangeEvent;
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

    @Override
    public void update(ExperienceChangeEvent event) {
        Experience experience = event.experience();
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


            try {
                LongSupplier channelId = event.channelId();

                MessageChannel channel = this.getChannelById(channelId);

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
