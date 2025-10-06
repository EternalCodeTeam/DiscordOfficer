package com.eternalcode.discordapp.ticket;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TicketRepository extends AbstractRepository<TicketWrapper, Long> {

    private TicketRepository(DatabaseManager databaseManager) {
        super(databaseManager, TicketWrapper.class);
    }

    public static TicketRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), TicketWrapper.class);
        }
        catch (SQLException exception) {
            Sentry.captureException(exception);
            throw new DataAccessException("Failed to create tickets table", exception);
        }
        return new TicketRepository(databaseManager);
    }

    public CompletableFuture<TicketWrapper> saveTicket(TicketWrapper ticket) {
        return this.save(ticket).thenApply(status -> ticket);
    }

    public CompletableFuture<Optional<TicketWrapper>> findByChannelId(long channelId) {
        return this.action(dao -> dao.queryBuilder()
                .where()
                .eq("channel_id", channelId)
                .queryForFirst())
            .thenApply(Optional::ofNullable);
    }

    public CompletableFuture<List<TicketWrapper>> findByUserId(long userId) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .eq("user_id", userId)
            .query());
    }

    public CompletableFuture<List<TicketWrapper>> findInactiveTickets(Instant cutoffTime) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .le("created_at", cutoffTime.getEpochSecond())
            .query());
    }

    public CompletableFuture<Integer> countTicketsByUser(long userId) {
        return this.action(dao -> (int) dao.queryBuilder()
            .where()
            .eq("user_id", userId)
            .countOf());
    }

    public CompletableFuture<Integer> countTicketsByUserAndCategory(long userId, String category) {
        return this.action(dao -> (int) dao.queryBuilder()
            .where()
            .eq("user_id", userId)
            .and()
            .eq("category", category)
            .countOf());
    }

    public CompletableFuture<Integer> deleteTicket(long id) {
        return this.deleteById(id);
    }
}
