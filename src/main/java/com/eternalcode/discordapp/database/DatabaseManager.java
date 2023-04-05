package com.eternalcode.discordapp.database;

import com.eternalcode.discordapp.config.DatabaseConfig;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final DatabaseConfig config;

    private HikariDataSource hikariDataSource;
    private ConnectionSource connectionSource;

    private final Map<Class<?>, Dao<?, ?>> daoCache = new ConcurrentHashMap<>();
    private final File folder;

    public DatabaseManager(DatabaseConfig config, File folder) {
        this.folder = folder;
        this.config = config;
    }

    public void connect() throws SQLException {

        this.hikariDataSource = new HikariDataSource();

        this.hikariDataSource.addDataSourceProperty("cachePrepStmts", true);
        this.hikariDataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.hikariDataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.hikariDataSource.addDataSourceProperty("useServerPrepStmts", true);

        this.hikariDataSource.setMaximumPoolSize(5);
        this.hikariDataSource.setUsername(this.config.username);
        this.hikariDataSource.setPassword(this.config.password);

        switch (this.config.type) {
            case MYSQL -> {
                this.hikariDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                this.hikariDataSource.setJdbcUrl("jdbc:mysql://" + this.config.host + ":" + this.config.port + "/" + this.config.database);
            }
            case MARIA_DB -> {
                this.hikariDataSource.setDriverClassName("org.mariadb.jdbc.Driver");
                this.hikariDataSource.setJdbcUrl("jdbc:mariadb://" + this.config.host + ":" + this.config.port + "/" + this.config.database);
            }
            case POSTGRESQL -> {
                this.hikariDataSource.setDriverClassName("org.postgresql.Driver");
                this.hikariDataSource.setJdbcUrl("jdbc:postgresql://" + this.config.host + ":" + this.config.port + "/" + this.config.database);
            }
            case H2 -> {
                this.hikariDataSource.setDriverClassName("org.h2.Driver");
                this.hikariDataSource.setJdbcUrl("jdbc:h2:" + this.folder.getAbsolutePath() + "/database");
            }
            default -> throw new SQLException("Database type not supported: " + this.config.type);
        }

        this.connectionSource = new DataSourceConnectionSource(this.hikariDataSource, this.hikariDataSource.getJdbcUrl());
    }

    public void closeConnection() throws Exception {
        this.hikariDataSource.close();
        this.connectionSource.close();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    @SuppressWarnings("unchecked")
    public <T, ID> Dao<T, ID> getDao(Class<T> clazz) throws SQLException {
        Dao<?, ?> dao = this.daoCache.get(clazz);

        if (dao == null) {
            dao = DaoManager.createDao(this.connectionSource, clazz);
            this.daoCache.put(clazz, dao);
        }

        return (Dao<T, ID>) dao;
    }

}
