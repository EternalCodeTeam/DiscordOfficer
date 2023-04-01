package com.eternalcode.discordapp.config;

import com.eternalcode.discordapp.database.DatabaseType;
import net.dzikoysk.cdn.entity.Description;
import net.dzikoysk.cdn.source.Resource;
import net.dzikoysk.cdn.source.Source;

import java.io.File;

public class DatabaseConfig implements CdnConfig {

    @Description("# Database settings")
    @Description("# Type of database server")
    public DatabaseType type = DatabaseType.H2;

    @Description({ "# SQL Drivers and ports:", "# MySQL (3306), MariaDB (3306), PostgresQL (5432)", "# H2" })
    public String host = "localhost";
    public int port = 3306;
    public String database = "";
    public String password = "";
    public String username = "";

    @Override
    public Resource resource(File folder) {
        return Source.of(folder, "database.yml");
    }

}
