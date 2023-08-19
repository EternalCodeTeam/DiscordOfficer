package com.eternalcode.discordapp.leveling.experience;

import java.util.List;
import java.util.concurrent.CompletableFuture;

interface ExperienceRepository {
    CompletableFuture<Experience> find(long id);

    CompletableFuture<Experience> saveExperience(Experience experience);

    CompletableFuture<Experience> modifyPoints(long id, double points, boolean add);

    CompletableFuture<List<Experience>> findAll();

    CompletableFuture<List<Experience>> getTop(int limit, long offset);
}
