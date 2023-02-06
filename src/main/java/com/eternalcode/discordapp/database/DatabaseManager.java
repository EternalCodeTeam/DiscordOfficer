package com.eternalcode.discordapp.database;

import com.eternalcode.discordapp.config.DiscordAppDatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final DiscordAppDatabaseConfig config;
    private ConnectionSource connectionSource;

    private final Map<Class<?>, Dao<?, ?>> daoCache = new ConcurrentHashMap<>();
    private final File folder;
    public DatabaseManager(DiscordAppDatabaseConfig config, File folder) {
        this.folder = folder;
        this.config = config;
    }

    public void connect() throws SQLException {
        switch (config.type) {
            case MYSQL -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:mysql://" + config.host + ":" + config.port + "/" + config.database, config.username, config.password);
            }
            case MARIA_DB -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:mariadb://" + config.host + ":" + config.port + "/" + config.database, config.username, config.password);
            }
            case POSTGRESQL -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:postgresql://" + config.host + ":" + config.port + "/" + config.database, config.username, config.password);
            }
            case H2 -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:h2:" + this.folder.getAbsolutePath() + "/database");
            }
            default -> throw new SQLException("Database type not supported: " + config.type);
        }
    }

    public void closeConnection() throws SQLException {
        this.connectionSource.close();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {
        try {
            Dao<?, ?> dao = this.daoCache.get(clazz);

            if(dao == null) {
                dao = DaoManager.createDao(this.connectionSource, clazz);
                this.daoCache.put(clazz, dao);
            }

            return (Dao<T, ID>) dao;
        } catch (SQLException exception) {
            throw new SQLException("Failed to get DAO for class: " + clazz.getName(), exception);
        }
    }

}
