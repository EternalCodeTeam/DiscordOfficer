package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.j256.ormlite.dao.Dao;
import panda.std.function.ThrowingFunction;
import panda.std.reactive.Completable;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractRepository<T, ID> {
    protected final DatabaseManager databaseManager;
    protected final Class<T> type;

    protected AbstractRepository(DatabaseManager databaseManager, Class<T> type) {
        this.databaseManager = databaseManager;
        this.type = type;
    }

    Completable<Dao.CreateOrUpdateStatus> save(T data) throws SQLException {
        return this.action(this.type, dao -> dao.createOrUpdate(data));
    }

    Completable<T> select(ID id) throws SQLException {
        return this.action(this.type, dao -> dao.queryForId(id));
    }

    Completable<Integer> delete(T data) throws SQLException {
        return this.action(this.type, dao -> dao.delete(data));
    }

    Completable<Integer> deleteById(ID id) throws SQLException {
        return this.action(this.type, dao -> dao.deleteById(id));
    }

    Completable<List<T>> selectAll() throws SQLException {
        return this.action(this.type, Dao::queryForAll);
    }

    <R> Completable<R> action(Class<T> type, ThrowingFunction<Dao<T, ID>, R, SQLException> action) throws SQLException {
        Completable<R> completableFuture = new Completable<>();

        Dao<T, ID> dao = this.databaseManager.getDao(type);

        try {
            completableFuture.complete(action.apply(dao));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return completableFuture;
    }

}
