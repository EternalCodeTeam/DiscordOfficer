package com.eternalcode.discordapp.experience;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public CompletableFuture<Experience> find(long id) {
        return this.select(id).thenApply(experienceWrapperOptional -> experienceWrapperOptional
                .map(ExperienceWrapper::toExperience)
                .orElse(new Experience(id, 0))
        );
    }

    @Override
    public CompletableFuture<Dao.CreateOrUpdateStatus> saveExperience(Experience user) {
        return this.save(ExperienceWrapper.from(user));
    }

    @Override
    public CompletableFuture<Dao.CreateOrUpdateStatus> modifyPoints(long id, double points, boolean add) {
        return this.find(id).thenCompose(experience -> {
            if (add) {
                experience.addPoints(points);
            }
            else {
                experience.removePoints(points);
            }

            return this.saveExperience(experience);
        });
    }

    @Override
    public CompletableFuture<List<Experience>> findAll() {
        return this.selectAll().thenApply(experienceWrappers -> experienceWrappers.stream()
                .map(ExperienceWrapper::toExperience)
                .collect(Collectors.toList())
        );
    }

}
