package com.eternalcode.discordapp.feature.meeting;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MeetingVoteRepository extends AbstractRepository<MeetingVoteWrapper, String> {

    protected MeetingVoteRepository(DatabaseManager databaseManager) {
        super(databaseManager, MeetingVoteWrapper.class);
    }

    public static MeetingVoteRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), MeetingVoteWrapper.class);
        }
        catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new com.eternalcode.discordapp.database.DataAccessException(
                "Failed to create meeting_vote table",
                sqlException
            );
        }
        return new MeetingVoteRepository(databaseManager);
    }

    public CompletableFuture<MeetingVoteWrapper> saveVote(MeetingVoteWrapper vote) {
        return this.save(vote).thenApply(status -> vote);
    }

    public CompletableFuture<List<MeetingVoteWrapper>> findByMessageId(long messageId) {
        return this.action(dao -> dao.queryBuilder().where().eq("message_id", messageId).query());
    }

    public CompletableFuture<Boolean> hasVoted(long messageId, long userId) {
        String id = messageId + ":" + userId;
        return this.select(id).thenApply(Optional::isPresent);
    }

    public CompletableFuture<Optional<MeetingVoteWrapper>> findUserVote(long messageId, long userId) {
        String id = messageId + ":" + userId;
        return this.select(id);
    }

    public CompletableFuture<Integer> deleteByMessageId(long messageId) {
        return this.action(dao -> {
            var delete = dao.deleteBuilder();
            delete.where().eq("message_id", messageId);
            return delete.delete();
        });
    }
}

