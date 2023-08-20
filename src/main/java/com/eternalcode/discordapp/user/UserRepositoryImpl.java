package com.eternalcode.discordapp.user;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserRepositoryImpl extends AbstractRepository<UserWrapper, Long> implements UserRepository {

    protected UserRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, UserWrapper.class);
    }

    public static UserRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), UserWrapper.class);
        }
        catch (SQLException sqlException) {
            throw new UserRepositoryException("Failed to create table", sqlException);
        }

        return new UserRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Optional<User>> findUser(Long id) {
        return this.select(id).thenApply(userWrapperOptional -> userWrapperOptional.map(UserWrapper::toUser));
    }

    @Override
    public CompletableFuture<Dao.CreateOrUpdateStatus> saveUser(User userWrapper) {
        return this.save(UserWrapper.from(userWrapper));
    }

    @Override
    public CompletableFuture<Integer> deleteUser(User userWrapper) {
        return this.delete(UserWrapper.from(userWrapper));
    }

    @Override
    public CompletableFuture<List<User>> selectAllUsers() {
        return this.selectAll().thenApply(users -> users.stream().map(UserWrapper::toUser).toList());
    }

    @Override
    public CompletableFuture<Integer> deleteUserById(Long id) {
        return this.deleteById(id);
    }


}
