package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.j256.ormlite.dao.Dao;
import panda.std.function.ThrowingFunction;
import panda.std.reactive.Completable;

import java.sql.SQLException;
import java.util.List;

abstract class AbstractRepository {
    protected final DatabaseManager databaseManager;

    protected AbstractRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    <T> Completable<Dao.CreateOrUpdateStatus> save(Class<T> type, T data) throws SQLException {
        return this.action(type, dao -> dao.createOrUpdate(data));
    }

    <T> Completable<T> saveIfNotExist(Class<T> type, T data) throws SQLException {
        return this.action(type, dao -> dao.createIfNotExists(data));
    }

    <T, ID> Completable<T> select(Class<T> type, ID id) throws SQLException {
        return this.action(type, dao -> dao.queryForId(id));
    }

    <T> Completable<Integer> delete(Class<T> type, T data) throws SQLException {
        return this.action(type, dao -> dao.delete(data));
    }

    <T, ID> Completable<Integer> deleteById(Class<T> type, ID id) throws SQLException {
        return this.action(type, dao -> dao.deleteById(id));
    }

    <T> Completable<List<T>> selectAll(Class<T> type) throws SQLException {
        return this.action(type, Dao::queryForAll);
    }

    <T, ID, R> Completable<R> action(Class<T> type, ThrowingFunction<Dao<T, ID>, R, SQLException> action) throws SQLException {
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
