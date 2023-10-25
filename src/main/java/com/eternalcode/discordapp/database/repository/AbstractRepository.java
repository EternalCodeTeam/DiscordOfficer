package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.j256.ormlite.dao.Dao;
import io.sentry.Sentry;
import panda.std.function.ThrowingFunction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractRepository<T, ID> {

    protected final DatabaseManager databaseManager;
    protected final Class<T> type;

    protected AbstractRepository(DatabaseManager databaseManager, Class<T> type) {
        this.databaseManager = databaseManager;
        this.type = type;
    }

    public CompletableFuture<Dao.CreateOrUpdateStatus> save(T data) {
        return this.action(dao -> dao.createOrUpdate(data));
    }

    public CompletableFuture<Optional<T>> select(ID id) {
        return this.action(dao -> Optional.ofNullable(dao.queryForId(id)));
    }

    public CompletableFuture<Integer> delete(T data) {
        return this.action(dao -> dao.delete(data));
    }

    public CompletableFuture<Integer> deleteById(ID id) {
        return this.action(dao -> dao.deleteById(id));
    }

    public CompletableFuture<List<T>> selectAll() {
        return this.action(Dao::queryForAll);
    }

    public <R> CompletableFuture<R> action(ThrowingFunction<Dao<T, ID>, R, SQLException> action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dao<T, ID> dao = this.databaseManager.getDao(this.type);
                return action.apply(dao);
            }
            catch (SQLException sqlException) {
                Sentry.captureException(sqlException);
                throw new DataAccessException("Failed to execute database action", sqlException);
            }
        });
    }

}
