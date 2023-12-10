package com.eternalcode.discordapp.meeting.database;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.meeting.Meeting;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MeetingRepositoryImpl extends AbstractRepository<Meeting, Long> implements MeetingRepository {

    protected MeetingRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, Meeting.class);
    }

    public static MeetingRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), MeetingWrapper.class);
        }
        catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new DataAccessException("Failed to create table", sqlException);
        }

        return new MeetingRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Meeting> findMeeting(Meeting meeting) {
        return this.select(meeting.getRequesterId()).thenApply(Optional::get);
    }

    @Override
    public CompletableFuture<Meeting> saveMeeting(Meeting meeting) {
        return this.save(meeting).thenApply(status -> meeting);
    }

    @Override
    public CompletableFuture<Meeting> deleteMeeting(Meeting meeting) {
        return this.delete(meeting).thenApply(deleted -> meeting);
    }
}
