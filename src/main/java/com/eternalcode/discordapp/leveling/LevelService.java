package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.database.DatabaseManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LevelService {

    private final LevelRepository levelRepository;

    public LevelService(DatabaseManager databaseManager) {
        this.levelRepository = LevelRepositoryImpl.create(databaseManager);
    }

    public void saveLevel(Level level) {
        this.levelRepository.saveLevel(level);
    }

    public CompletableFuture<Level> find(long id) {
        return this.levelRepository.find(id);
    }

    public CompletableFuture<List<Level>> getTop(int limit, long offset) {
        return this.levelRepository.getTop(limit, offset);
    }

    public CompletableFuture<Integer> getTotalRecordsCount() {
        return this.levelRepository.getTotalRecordsCount();
    }

}
