package com.eternalcode.discordapp.feature.ticket;

import static com.eternalcode.discordapp.feature.ticket.TicketWrapper.COLUMN_CATEGORY;
import static com.eternalcode.discordapp.feature.ticket.TicketWrapper.COLUMN_CHANNEL_ID;
import static com.eternalcode.discordapp.feature.ticket.TicketWrapper.COLUMN_CREATED_AT;
import static com.eternalcode.discordapp.feature.ticket.TicketWrapper.COLUMN_USER_ID;

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
                .eq(COLUMN_CHANNEL_ID, channelId)
                .queryForFirst())
            .thenApply(Optional::ofNullable);
    }

    public CompletableFuture<List<TicketWrapper>> findByUserId(long userId) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .eq(COLUMN_USER_ID, userId)
            .query());
    }

    public CompletableFuture<List<TicketWrapper>> findTicketsOlderThan(Instant cutoffTime) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .le(COLUMN_CREATED_AT, cutoffTime.getEpochSecond())
            .query());
    }

    public CompletableFuture<Long> countTicketsByUser(long userId) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .eq(COLUMN_USER_ID, userId)
            .countOf());
    }

    public CompletableFuture<Long> countTicketsByUserAndCategory(long userId, String category) {
        return this.action(dao -> dao.queryBuilder()
            .where()
            .eq(COLUMN_USER_ID, userId)
            .and()
            .eq(COLUMN_CATEGORY, category)
            .countOf());
    }

    public CompletableFuture<Integer> deleteTicket(long id) {
        return this.deleteById(id);
    }
}

