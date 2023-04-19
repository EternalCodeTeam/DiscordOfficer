package com.eternalcode.discordapp.experience;

import com.j256.ormlite.dao.Dao;

import java.util.concurrent.CompletableFuture;

public interface ExperienceRepository {

    CompletableFuture<Experience> find(long id);

    CompletableFuture<Dao.CreateOrUpdateStatus> saveExperience(Experience user);
}
