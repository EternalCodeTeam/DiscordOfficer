package com.eternalcode.discordapp.leveling.games.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.leveling.experience.ExperienceService;
import com.eternalcode.discordapp.leveling.games.CodeImageGameData;
import com.eternalcode.discordapp.leveling.games.configuration.CodeGameConfiguration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import panda.utilities.text.Formatter;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class CodeGameAnswerListener extends ListenerAdapter {

    private static final Random RANDOM_CODE = new Random();

    private final CodeImageGameData codeImageGameData;
    private final CodeGameConfiguration codeGameConfiguration;
    private final ConfigManager dataManager;
    private final ExperienceService experienceService;

    public CodeGameAnswerListener(CodeImageGameData codeImageGameData, CodeGameConfiguration codeGameConfiguration, ConfigManager dataManager, ExperienceService experienceService) {
        this.codeImageGameData = codeImageGameData;
        this.codeGameConfiguration = codeGameConfiguration;
        this.dataManager = dataManager;
        this.experienceService = experienceService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() != this.codeGameConfiguration.channelId) {
            return;
        }

        if (!this.codeImageGameData.gameActive) {
            return;
        }

        if (this.codeImageGameData.isUsed) {
            return;
        }

        if (event.getMessage().getContentRaw().equalsIgnoreCase(this.codeImageGameData.code)) {
            this.codeImageGameData.isUsed = true;
            this.codeImageGameData.gameActive = false;
            this.dataManager.save(this.codeImageGameData);

            int points = RANDOM_CODE.nextInt(this.codeGameConfiguration.maxPoints) + 1;
            Instant now = Instant.now();
            long timeDifferenceMinutes = Duration.between(this.codeImageGameData.lastUpdated, now).toMinutes();

            Formatter formatter = new Formatter()
                .register("{winner}", event.getAuthor().getAsMention())
                .register("{points}", points)
                .register("{time}", timeDifferenceMinutes);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(this.codeGameConfiguration.embedSettings.title)
                .setColor(Color.decode(this.codeGameConfiguration.embedSettings.color))
                .setDescription(formatter.format(this.codeGameConfiguration.embedSettings.description))
                .setFooter(this.codeGameConfiguration.embedSettings.footer);

            this.experienceService.modifyPoints(event.getAuthor().getIdLong(), points, true)
                .whenComplete((experience, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                });

            event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }
}