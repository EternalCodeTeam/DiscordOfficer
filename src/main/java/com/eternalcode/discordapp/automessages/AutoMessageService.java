package com.eternalcode.discordapp.automessages;

import com.eternalcode.commons.concurrent.FutureHandler;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoMessageService.class);

    private final JDA jda;
    private final AutoMessagesConfig config;
    private final Map<Long, Instant> lastMessageTimes = new ConcurrentHashMap<>();

    public AutoMessageService(JDA jda, AutoMessagesConfig config) {
        this.jda = jda;
        this.config = config;
        LOGGER.info("AutoMessageService initialized with {} entries", config.entries.size());
    }

    public CompletableFuture<AutoMessageResults> sendAutoMessages() {
        if (config.entries.isEmpty()) {
            LOGGER.debug("No auto messages configured");
            return CompletableFuture.completedFuture(new AutoMessageResults(0, 0));
        }

        LOGGER.info("Starting auto message sending - {} entries", config.entries.size());

        List<CompletableFuture<Boolean>> futures = config.entries.stream()
            .map(this::sendAutoMessage)
            .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

        return allFutures.thenApply(ignored -> {
            int successful = (int) futures.stream()
                .map(CompletableFuture::join)
                .filter(Boolean::booleanValue)
                .count();

            int failed = futures.size() - successful;
            LOGGER.info("Auto messages completed: {}/{} successful", successful, futures.size());

            return new AutoMessageResults(successful, failed);
        }).exceptionally(FutureHandler::handleException);
    }

    private CompletableFuture<Boolean> sendAutoMessage(AutoMessagesConfig.AutoMessagesEntry entry) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TextChannel channel = jda.getTextChannelById(entry.channelId);
                if (channel == null) {
                    LOGGER.warn("Channel not found: {}", entry.channelId);
                    return false;
                }

                if (shouldSkipSending(channel, entry)) {
                    LOGGER.debug("Skipping message to #{} - recent duplicate found", channel.getName());
                    return false;
                }

                String messageToSend = selectRandomMessage(entry.messages);

                return channel.sendMessage(messageToSend)
                    .submit()
                    .thenApply(message -> {
                        lastMessageTimes.put(entry.channelId, Instant.now());
                        LOGGER.info("✅ Auto message sent to #{}", channel.getName());
                        return true;
                    })
                    .exceptionally(error -> {
                        LOGGER.error("Failed to send message to #{}: {}", channel.getName(), error.getMessage());
                        return false;
                    })
                    .join();
            }
            catch (Exception exception) {
                LOGGER.error(
                    "Error processing auto message for channel {}: {}",
                    entry.channelId,
                    exception.getMessage());
                return false;
            }
        }).exceptionally(FutureHandler::handleException);
    }

    private boolean shouldSkipSending(TextChannel channel, AutoMessagesConfig.AutoMessagesEntry entry) {
        try {
            List<Message> recentMessages = channel.getHistory()
                .retrievePast(config.duplicateCheckCount)
                .complete();

            String botId = jda.getSelfUser().getId();
            Set<String> messagesToCheck = Set.of(entry.messages.toArray(String[]::new));

            return recentMessages.stream()
                .filter(message -> message.getAuthor().getId().equals(botId))
                .anyMatch(message -> messagesToCheck.contains(message.getContentRaw()));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to check recent messages in #{}: {}", channel.getName(), exception.getMessage());
            return false;
        }
    }

    private String selectRandomMessage(List<String> messages) {
        if (messages.size() == 1) {
            return messages.get(0);
        }
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
    }

    public record AutoMessageResults(int successful, int failed) {
        public int total() {
            return successful + failed;
        }
    }
}
