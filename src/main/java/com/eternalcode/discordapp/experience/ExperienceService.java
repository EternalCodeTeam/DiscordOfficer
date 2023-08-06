package com.eternalcode.discordapp.experience;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.observer.ObserverRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ObserverRegistry observerRegistry;

    public ExperienceService(DatabaseManager databaseManager, ObserverRegistry observerRegistry) {
        this.experienceRepository = ExperienceRepositoryImpl.create(databaseManager);
        this.observerRegistry = observerRegistry;
    }

    public CompletableFuture<Experience> saveExperience(Experience experience) {
        return this.experienceRepository.saveExperience(experience).whenComplete((experience1, throwable) -> {
            if (throwable == null) {
                this.observerRegistry.publish(new ExperienceChangeEvent(experience1));
            }
        });
    }

    public CompletableFuture<Experience> modifyPoints(long id, double points, boolean add) {
        return this.experienceRepository.modifyPoints(id, points, add).whenComplete((experience, throwable) -> {
            if (throwable == null) {
                this.observerRegistry.publish(new ExperienceChangeEvent(experience));
            }
        });
    }

    public CompletableFuture<List<Experience>> getTop(int limit, long offset) {
        return this.experienceRepository.getTop(limit, offset);
    }

}
