package com.eternalcode.discordapp.database.repository.user;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.model.User;
import com.eternalcode.discordapp.database.wrapper.UserWrapper;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import panda.std.reactive.Completable;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepositoryImpl extends AbstractRepository<UserWrapper, Long> implements UserRepository {
    protected UserRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, UserWrapper.class);
    }

    public static UserRepositoryImpl create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), UserWrapper.class);
        }
        catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return new UserRepositoryImpl(databaseManager);
    }

    @Override
    public Completable<User> findUser(Long id) {
        return this.select(id).thenApply(UserWrapper::toUser);
    }

    @Override
    public Completable<Dao.CreateOrUpdateStatus> saveUser(User userWrapper) {
        return this.save(UserWrapper.from(userWrapper));
    }

    @Override
    public Completable<Integer> deleteUser(User userWrapper) {
        return this.delete(UserWrapper.from(userWrapper));
    }

    @Override
    public Completable<List<User>> selectAllUsers() {
        return this.selectAll().thenApply(users -> users.stream().map(UserWrapper::toUser).collect(Collectors.toList()));
    }

    @Override
    public Completable<Integer> deleteUserById(Long id) {
        return this.deleteById(id);
    }
}
