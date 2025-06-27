package com.eternalcode.discordapp.automessages;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AutoMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoMessageService.class);
    private static final int CHECK_LAST_MESSAGES_COUNT = 10;

    private final JDA jda;
    private final AppConfig config;

    public AutoMessageService(JDA jda, AppConfig config) {
        this.jda = jda;
        this.config = config;
    }

    public void sendAutoMessages() {
        if (config.autoMessages.entries.isEmpty()) {
            LOGGER.debug("No auto messages configured");
            return;
        }

        LOGGER.info("Starting auto message sending process...");

        for (AppConfig.AutoMessages.AutoMessagesEntry entry : config.autoMessages.entries) {
            try {
                sendAutoMessage(entry);
            } catch (Exception exception) {
                LOGGER.error("Failed to send auto message to channel {}: {}", entry.channelId, exception.getMessage(), exception);
            }
        }
    }

    private void sendAutoMessage(AppConfig.AutoMessages.AutoMessagesEntry entry) {
        TextChannel channel = jda.getTextChannelById(entry.channelId);
        
        if (channel == null) {
            LOGGER.warn("Channel with ID {} not found", entry.channelId);
            return;
        }

        if (shouldSkipSending(channel, entry.message)) {
            LOGGER.debug("Skipping auto message to channel {} - last messages are already auto messages", channel.getName());
            return;
        }

        channel.sendMessage(entry.message).queue(
            message -> LOGGER.info("Auto message sent to channel {}: {}", channel.getName(), entry.message),
            error -> LOGGER.error("Failed to send auto message to channel {}: {}", channel.getName(), error.getMessage())
        );
    }

    private boolean shouldSkipSending(TextChannel channel, String messageContent) {
        try {
            List<Message> recentMessages = channel.getHistory()
                .retrievePast(CHECK_LAST_MESSAGES_COUNT)
                .complete();

            for (Message message : recentMessages) {
                if (message.getAuthor().isBot() && message.getContentRaw().equals(messageContent)) {
                    return true; 
                }
            }

            return false;
        } catch (Exception exception) {
            LOGGER.warn("Failed to check recent messages in channel {}: {}", channel.getName(), exception.getMessage());
            return false; 
        }
    }
} 