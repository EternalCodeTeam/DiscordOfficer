package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;

public class RepositoryManager {
    private final DatabaseManager databaseManager;

    private UserRepository userRepository;

    public RepositoryManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void init() {
        this.userRepository = UserRepository.create(this.databaseManager);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
