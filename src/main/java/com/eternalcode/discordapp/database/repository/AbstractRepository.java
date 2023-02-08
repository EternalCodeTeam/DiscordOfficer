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

    public Completable<Dao.CreateOrUpdateStatus> save(T data) {
        return this.action(this.type, dao -> dao.createOrUpdate(data));
    }

    public Completable<T> select(ID id) {
        return this.action(this.type, dao -> dao.queryForId(id));
    }

    public Completable<Integer> delete(T data) {
        return this.action(this.type, dao -> dao.delete(data));
    }

    public Completable<Integer> deleteById(ID id) {
        return this.action(this.type, dao -> dao.deleteById(id));
    }

    public Completable<List<T>> selectAll() {
        return this.action(this.type, Dao::queryForAll);
    }

    public <R> Completable<R> action(Class<T> type, ThrowingFunction<Dao<T, ID>, R, SQLException> action) {
        Completable<R> completableFuture = new Completable<>();

        try {
            Dao<T, ID> dao = this.databaseManager.getDao(type);
            completableFuture.complete(action.apply(dao));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return completableFuture;
    }

}
