package com.eternalcode.discordapp.database.repository.userpoints;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.model.UserPoints;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.database.wrapper.UserPointsWrapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import panda.std.reactive.Completable;

import java.sql.SQLException;

public class UserPointsRepositoryImpl extends AbstractRepository<UserPointsWrapper, Long> implements UserPointsRepository {

    protected UserPointsRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, UserPointsWrapper.class);
    }

    public static UserPointsRepositoryImpl create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), UserPointsWrapper.class);
        }
        catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return new UserPointsRepositoryImpl(databaseManager);
    }

    @Override
    public Completable<UserPoints> findUser(Long id) {
        return this.select(id).thenApply(UserPointsWrapper::toUserPoints);
    }

    @Override
    public Completable<Dao.CreateOrUpdateStatus> saveUser(UserPoints user) {
        return this.save(UserPointsWrapper.from(user));
    }

    @Override
    public Completable<Integer> deleteUser(UserPoints user) {
        return this.delete(UserPointsWrapper.from(user));
    }

    @Override
    public Completable<Integer> deleteUserById(Long id) {
        return this.deleteById(id);
    }
}
