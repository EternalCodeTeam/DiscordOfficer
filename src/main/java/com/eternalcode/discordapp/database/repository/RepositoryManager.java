package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.user.UserRepositoryImpl;
import com.eternalcode.discordapp.database.repository.userpoints.UserPointsRepositoryImpl;

public class RepositoryManager {
    private final DatabaseManager databaseManager;

    private UserRepositoryImpl userRepository;
    private UserPointsRepositoryImpl userPointsRepository;

    public RepositoryManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void init() {
        this.userRepository = UserRepositoryImpl.create(this.databaseManager);
        this.userPointsRepository = UserPointsRepositoryImpl.create(this.databaseManager);
    }

    public UserRepositoryImpl getUserRepository() {
        return userRepository;
    }

    public UserPointsRepositoryImpl getUserPointsRepository() {
        return userPointsRepository;
    }
}
