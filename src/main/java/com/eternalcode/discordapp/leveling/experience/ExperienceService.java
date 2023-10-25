package com.eternalcode.discordapp.leveling.experience;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.observer.ObserverRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ObserverRegistry observerRegistry;

    public ExperienceService(DatabaseManager databaseManager, ObserverRegistry observerRegistry) {
        this.experienceRepository = ExperienceRepositoryImpl.create(databaseManager);
        this.observerRegistry = observerRegistry;
    }

    public CompletableFuture<Experience> saveExperience(Experience experience, LongSupplier channelId) {
        return this.experienceRepository.saveExperience(experience).whenComplete((experience1, throwable) -> {
            if (throwable == null) {
                this.observerRegistry.publish(new ExperienceChangeEvent(experience1, channelId));
            }
        });
    }

    public CompletableFuture<Experience> modifyPoints(long userId, double points, boolean add, LongSupplier channelId) {
        return this.experienceRepository.modifyPoints(userId, points, add).whenComplete((experience, throwable) -> {
            if (throwable == null) {
                this.observerRegistry.publish(new ExperienceChangeEvent(experience, channelId));
            }
        });
    }

    public CompletableFuture<List<Experience>> getTop(int limit, long offset) {
        return this.experienceRepository.getTop(limit, offset);
    }

}
