package com.eternalcode.discordapp.database;

import com.eternalcode.discordapp.config.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final DatabaseConfig config;
    private ConnectionSource connectionSource;

    private final Map<Class<?>, Dao<?, ?>> daoCache = new ConcurrentHashMap<>();
    private final File folder;

    public DatabaseManager(DatabaseConfig config, File folder) {
        this.folder = folder;
        this.config = config;
    }

    public void connect() throws SQLException {
        switch (this.config.type) {
            case MYSQL -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:mysql://" + this.config.host + ":" + this.config.port + "/" + this.config.database, this.config.username, this.config.password);
            }
            case MARIA_DB -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:mariadb://" + this.config.host + ":" + this.config.port + "/" + this.config.database, this.config.username, this.config.password);
            }
            case POSTGRESQL -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:postgresql://" + this.config.host + ":" + this.config.port + "/" + this.config.database, this.config.username, this.config.password);
            }
            case H2 -> {
                this.connectionSource = new JdbcConnectionSource("jdbc:h2:" + this.folder.getAbsolutePath() + "/database");
            }
            default -> throw new SQLException("Database type not supported: " + this.config.type);
        }
    }

    public void closeConnection() throws Exception {
        this.connectionSource.close();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {
        try {
            Dao<?, ?> dao = this.daoCache.get(clazz);

            if (dao == null) {
                dao = DaoManager.createDao(this.connectionSource, clazz);
                this.daoCache.put(clazz, dao);
            }

            return (Dao<T, ID>) dao;
        }
        catch (SQLException exception) {
            throw new SQLException("Failed to get DAO for class: " + clazz.getName(), exception);
        }
    }

}
