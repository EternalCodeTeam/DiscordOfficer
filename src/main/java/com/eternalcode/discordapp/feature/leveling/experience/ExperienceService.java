package com.eternalcode.discordapp.feature.leveling.experience;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.observer.ObserverRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ObserverRegistry observerRegistry;

    public ExperienceService(DatabaseManager databaseManager, ObserverRegistry observerRegistry) {
        this.experienceRepository = ExperienceRepositoryImpl.create(databaseManager);
        this.observerRegistry = observerRegistry;
    }

    public CompletableFuture<Experience> modifyPoints(long userId, double points, boolean add, LongSupplier channelId) {
        return this.experienceRepository.modifyPoints(userId, points, add).whenComplete((experience, throwable) -> {
            if (throwable == null) {
                this.observerRegistry.publish(new ExperienceChangeEvent(experience, channelId));
            }
        });
    }

}

