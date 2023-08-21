package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LevelRepositoryImpl extends AbstractRepository<LevelWrapper, Long> implements LevelRepository {

    protected LevelRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, LevelWrapper.class);
    }

    public static LevelRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), LevelWrapper.class);
        }
        catch (SQLException sqlException) {
            throw new DataAccessException("Failed to create table", sqlException);
        }

        return new LevelRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Level> find(long id) {
        return this.select(id).thenApply(levelOptional ->
            levelOptional.map(LevelWrapper::toLevel)
                    .orElse(new Level(id, 0))
        );
    }

    @Override
    public CompletableFuture<List<Level>> getTop(int limit, long offset) {
        return this.action(dao -> dao.queryBuilder().orderBy("level", false).limit((long) limit).offset(offset).query())
                .thenApply(levels -> levels.stream().map(LevelWrapper::toLevel).toList());
    }

    @Override
    public CompletableFuture<Integer> getTotalRecordsCount() {
        return this.action(dao -> Math.toIntExact(dao.queryBuilder().countOf()));
    }

    @Override
    public CompletableFuture<Level> saveLevel(Level level) {
        this.save(LevelWrapper.from(level));

        return CompletableFuture.completedFuture(level);
    }
}
