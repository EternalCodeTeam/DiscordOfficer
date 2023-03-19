package com.eternalcode.discordapp.expierience;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class ExperienceRepositoryImpl extends AbstractRepository<ExperienceWrapper, Long> implements ExperienceRepository {

    protected ExperienceRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, ExperienceWrapper.class);
    }

    public static ExperienceRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), ExperienceWrapper.class);
        }
        catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return new ExperienceRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Experience> findUser(Long id) {
        return this.select(id).thenApply(experienceWrapperOptional -> experienceWrapperOptional
                .map(ExperienceWrapper::toUserPoints)
                .orElse(new Experience(id, 0))
        );
    }

    @Override
    public CompletableFuture<Dao.CreateOrUpdateStatus> saveUser(Experience user) {
        return this.save(ExperienceWrapper.from(user));
    }

    @Override
    public CompletableFuture<Integer> deleteUser(Experience user) {
        return this.delete(ExperienceWrapper.from(user));
    }

    @Override
    public CompletableFuture<Integer> deleteUserById(Long id) {
        return this.deleteById(id);
    }
}
