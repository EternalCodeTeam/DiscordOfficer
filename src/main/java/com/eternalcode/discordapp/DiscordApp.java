package com.eternalcode.discordapp;

import com.eternalcode.discordapp.command.*;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.config.DatabaseConfig;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.RepositoryManager;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.sql.SQLException;

public class DiscordApp {

    private static final boolean IS_DEVELOPER_MODE = false;
    private static AppConfig config;
    private static DatabaseManager databaseManager;
    private static DatabaseConfig databaseConfig;
    private static RepositoryManager repositoryManager;


    public static void main(String... args) {
        ConfigManager configManager = new ConfigManager(new File("config"));
        config = new AppConfig();
        databaseConfig = new DatabaseConfig();
        configManager.load(config);
        configManager.load(databaseConfig);

        try {
            databaseManager = new DatabaseManager(databaseConfig, new File("database"));
            databaseManager.connect();
            repositoryManager = new RepositoryManager(databaseManager);
            repositoryManager.init();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

        CommandClientBuilder builder = new CommandClientBuilder()
                .addSlashCommands(
                        new AvatarCommand(config),
                        new BanCommand(config),
                        new BotInfoCommand(config),
                        new ClearCommand(config),
                        new CooldownCommand(config),
                        new EmbedCommand(),
                        new KickCommand(config),
                        new PingCommand(config),
                        new ServerCommand(config))
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId)
                .setActivity(Activity.playing("IntelliJ IDEA"));
        CommandClient commandClient = builder.build();

        JDABuilder.createDefault(getToken())
                .addEventListeners(commandClient)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_BANS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.SCHEDULED_EVENTS
                )
                .build();
    }

    public static String getToken() {
        if (!IS_DEVELOPER_MODE) {
            return config.token;
        }

        return System.getenv("OFFICER_TOKEN");
    }
}