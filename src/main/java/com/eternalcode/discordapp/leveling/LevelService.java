package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.database.DatabaseManager;

import java.util.concurrent.CompletableFuture;

public class LevelService {

    private final LevelRepository levelRepository;

    public LevelService(DatabaseManager databaseManager) {
        this.levelRepository = LevelRepositoryImpl.create(databaseManager);
    }

    public CompletableFuture<Level> saveLevel(Level level) {
        return this.levelRepository.saveLevel(level);
    }

    public CompletableFuture<Level> find(long id) {
        return this.levelRepository.find(id);
    }
}
