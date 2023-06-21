package com.eternalcode.discordapp.experience;

import com.j256.ormlite.dao.Dao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ExperienceRepository {
    CompletableFuture<Experience> find(long id);

    CompletableFuture<Dao.CreateOrUpdateStatus> saveExperience(Experience user);

    CompletableFuture<Dao.CreateOrUpdateStatus> modifyPoints(long id, double points, boolean add);

    CompletableFuture<List<Experience>> findAll();
}
