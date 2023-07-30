package com.eternalcode.discordapp.leveling;

import java.util.concurrent.CompletableFuture;

interface LevelRepository {

    CompletableFuture<Level> find(long id);

    CompletableFuture<Level> saveLevel(Level level);
}
