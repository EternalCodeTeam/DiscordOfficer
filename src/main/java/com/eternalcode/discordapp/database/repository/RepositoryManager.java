package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.user.UserRepositoryImpl;

public class RepositoryManager {
    private final DatabaseManager databaseManager;

    private UserRepositoryImpl userRepository;

    public RepositoryManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void init() {
        this.userRepository = UserRepositoryImpl.create(this.databaseManager);
    }

    public UserRepositoryImpl getUserRepository() {
        return userRepository;
    }
}
