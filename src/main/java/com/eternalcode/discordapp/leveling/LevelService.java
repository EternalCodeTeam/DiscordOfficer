package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.database.DatabaseManager;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class LevelService {

    private final LevelRepository levelRepository;

    public LevelService(DatabaseManager databaseManager) {
        this.levelRepository = LevelRepositoryImpl.create(databaseManager);
    }


    public CompletableFuture<Void> generateRandomLevels(int count) {
        Random random = new Random();
        CompletableFuture<Void> future = new CompletableFuture<>();

        for (int i = 0; i < count; i++) {
            long id = random.nextLong();
            int level = random.nextInt(100);

            Level randomLevel = new Level(id, level);
            int currentIndex = i; // Create a separate variable to capture the current value of i

            saveLevel(randomLevel).thenAccept(savedLevel -> {
                System.out.println("Saved level: " + savedLevel.getId() + ", " + savedLevel.getLevel());
                if (currentIndex == count - 1) {
                    future.complete(null);
                }
            });
        }

        return future;
    }


    public CompletableFuture<Level> saveLevel(Level level) {
        return this.levelRepository.saveLevel(level);
    }

    public CompletableFuture<Level> find(long id) {
        return this.levelRepository.find(id);
    }

    public CompletableFuture<List<Level>> getTop(int limit, long offset) {
        return this.levelRepository.getTop(limit, offset);
    }
}
