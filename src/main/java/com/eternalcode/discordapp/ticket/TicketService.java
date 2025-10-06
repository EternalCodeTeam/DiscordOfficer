package com.eternalcode.discordapp.ticket;

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

    public CompletableFuture<List<TicketWrapper>> findInactiveTickets(Instant cutoffTime) {
        return this.repository.findInactiveTickets(cutoffTime);
    }

    public CompletableFuture<Integer> deleteTicket(long ticketId) {
        return this.repository.deleteTicket(ticketId);
    }

    public CompletableFuture<Integer> countTicketsByUser(long userId) {
        return this.repository.countTicketsByUser(userId);
    }

    public CompletableFuture<Integer> countTicketsByUserAndCategory(long userId, String categoryId) {
        return this.repository.countTicketsByUserAndCategory(userId, categoryId);
    }

    public boolean canUserCreateTicket(long userId, String categoryId) {
        try {
            int totalCount = this.countTicketsByUser(userId).join();
            if (totalCount >= this.config.maxTicketsPerUser) {
                return true;
            }

            TicketConfig.TicketCategoryConfig cat = this.config.getCategoryById(categoryId);
            if (cat == null) {
                return true;
            }

            int categoryCount = this.countTicketsByUserAndCategory(userId, categoryId).join();
            return categoryCount >= cat.maxPerUser;
        }
        catch (Exception exception) {
            LOGGER.error("Error checking ticket limits for user {}", userId, exception);
            return true;
        }
    }
}
