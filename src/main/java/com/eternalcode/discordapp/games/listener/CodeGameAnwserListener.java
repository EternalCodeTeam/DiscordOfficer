package com.eternalcode.discordapp.games.listener;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.experience.ExperienceService;
import com.eternalcode.discordapp.games.CodeImageGameData;
import com.eternalcode.discordapp.games.configuration.CodeGameConfiguration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import panda.utilities.text.Formatter;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class CodeGameAnwserListener extends ListenerAdapter {
    private final CodeImageGameData codeImageGameData;

    private final CodeGameConfiguration codeGameConfiguration;

    private final ConfigManager dataManager;
    private final ExperienceService experienceService;

    public CodeGameAnwserListener(CodeImageGameData codeImageGameData, CodeGameConfiguration codeGameConfiguration, ConfigManager dataManager, ExperienceService experienceService) {
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

            int points = new Random().nextInt(1, this.codeGameConfiguration.maxPoints + 1);

            Formatter formatter = new Formatter()
                    .register("{winner}", event.getAuthor().getAsMention())
                    .register("{points}", points)
                    .register("{time}", Duration.between(this.codeImageGameData.lastUpdated, Instant.now()).toMinutes());

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(this.codeGameConfiguration.embedSettings.title)
                    .setColor(Color.decode(this.codeGameConfiguration.embedSettings.color))
                    .setDescription(formatter.format(this.codeGameConfiguration.embedSettings.description))
                    .setFooter(this.codeGameConfiguration.embedSettings.footer);

            this.experienceService.modifyPoints(event.getAuthor().getIdLong(), points, true).whenComplete((experience, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            });

            event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }

    }
}
