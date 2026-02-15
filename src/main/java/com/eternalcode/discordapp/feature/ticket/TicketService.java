package com.eternalcode.discordapp.feature.ticket;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    private final TicketConfig config;
    private final TicketRepository repository;

    public TicketService(TicketConfig config, TicketRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    public CompletableFuture<TicketWrapper> saveTicket(TicketWrapper ticket) {
        return this.repository.saveTicket(ticket);
    }

    public CompletableFuture<Optional<TicketWrapper>> getTicketByChannel(long channelId) {
        return this.repository.findByChannelId(channelId);
    }

    public CompletableFuture<List<TicketWrapper>> getUserTickets(long userId) {
        return this.repository.findByUserId(userId);
    }

    public CompletableFuture<List<TicketWrapper>> findTicketsOlderThan(Instant cutoffTime) {
        return this.repository.findTicketsOlderThan(cutoffTime);
    }

    public CompletableFuture<Integer> deleteTicket(long ticketId) {
        return this.repository.deleteTicket(ticketId);
    }

    public CompletableFuture<Long> countTicketsByUser(long userId) {
        return this.repository.countTicketsByUser(userId);
    }

    public CompletableFuture<Long> countTicketsByUserAndCategory(long userId, String categoryId) {
        return this.repository.countTicketsByUserAndCategory(userId, categoryId);
    }

    public CompletableFuture<Boolean> canUserCreateTicket(long userId, String categoryId) {
        return this.countTicketsByUser(userId)
            .thenCompose(totalCount -> {
                if (totalCount >= this.config.maxTicketsPerUser) {
                    return CompletableFuture.completedFuture(false);
                }

                TicketConfig.TicketCategoryConfig categoryConfig = this.config.getCategoryById(categoryId);
                if (categoryConfig == null) {
                    return CompletableFuture.completedFuture(false);
                }

                return this.countTicketsByUserAndCategory(userId, categoryId)
                    .thenApply(categoryCount -> categoryCount < categoryConfig.maxPerUser);
            })
            .exceptionally(exception -> {
                LOGGER.error("Error checking ticket limits for user {}", userId, exception);
                return false;
            });
    }
}
