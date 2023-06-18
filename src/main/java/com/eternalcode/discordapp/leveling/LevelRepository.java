package com.eternalcode.discordapp.leveling;

import com.j256.ormlite.dao.Dao;

import java.util.concurrent.CompletableFuture;

public interface LevelRepository {

    CompletableFuture<Level> find(long id);

    CompletableFuture<Dao.CreateOrUpdateStatus> saveLevel(Level level);
}
