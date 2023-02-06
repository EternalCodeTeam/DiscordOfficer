package com.eternalcode.discordapp.database.repository;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.model.User;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class UserRepository extends AbstractRepository {
    protected UserRepository(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public static UserRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), User.class);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        return new UserRepository(databaseManager);
    }
}
