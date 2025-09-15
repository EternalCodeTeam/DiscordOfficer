package com.eternalcode.discordapp.meeting;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MeetingPollRepository extends AbstractRepository<MeetingPollWrapper, Long> {

    protected MeetingPollRepository(DatabaseManager databaseManager) {
        super(databaseManager, MeetingPollWrapper.class);
    }

    public static MeetingPollRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), MeetingPollWrapper.class);
        }
        catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new com.eternalcode.discordapp.database.DataAccessException(
                "Failed to create meeting_poll table",
                sqlException);
        }
        return new MeetingPollRepository(databaseManager);
    }

    public CompletableFuture<MeetingPollWrapper> savePoll(MeetingPollWrapper poll) {
        return this.save(poll).thenApply(status -> poll);
    }

    public CompletableFuture<Integer> deleteByMessageId(long messageId) {
        return this.deleteById(messageId);
    }

    public CompletableFuture<List<MeetingPollWrapper>> selectAllPolls() {
        return this.selectAll();
    }
}
