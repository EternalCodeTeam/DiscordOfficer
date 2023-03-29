package com.eternalcode.discordapp;


import com.eternalcode.discordapp.command.AvatarCommand;
import com.eternalcode.discordapp.command.BanCommand;
import com.eternalcode.discordapp.command.SayCommand;
import com.eternalcode.discordapp.command.ServerCommand;
import com.eternalcode.discordapp.command.BotInfoCommand;
import com.eternalcode.discordapp.command.ClearCommand;
import com.eternalcode.discordapp.command.CooldownCommand;
import com.eternalcode.discordapp.command.EmbedCommand;
import com.eternalcode.discordapp.command.KickCommand;
import com.eternalcode.discordapp.command.MinecraftServerInfoCommand;
import com.eternalcode.discordapp.command.PingCommand;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.config.DatabaseConfig;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.expierience.ExperienceRepository;
import com.eternalcode.discordapp.expierience.ExperienceRepositoryImpl;
import com.eternalcode.discordapp.expierience.ExperienceListener;
import com.eternalcode.discordapp.user.UserRepository;
import com.eternalcode.discordapp.user.UserRepositoryImpl;
import com.eternalcode.discordapp.filter.FilterMessageEmbedController;
import com.eternalcode.discordapp.filter.FilterService;
import com.eternalcode.discordapp.filter.renovate.RenovateForcedPushFilter;
import com.eternalcode.discordapp.guildstats.GuildStatisticsService;
import com.eternalcode.discordapp.guildstats.GuildStatisticsTask;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Timer;

public class DiscordApp {

    private static final boolean IS_DEVELOPER_MODE = false;
    private static AppConfig config;
    private static DatabaseConfig databaseConfig;


    private static DatabaseManager databaseManager;
    private static UserRepository userRepository;
    private static ExperienceRepository experienceRepository;

    public static void main(String... args) throws InterruptedException {
        ConfigManager configManager = new ConfigManager(new File("config"));
        configManager.load(new AppConfig());
        configManager.load(new DatabaseConfig());

        try {
            databaseManager = new DatabaseManager(databaseConfig, new File("database"));
            databaseManager.connect();
            userRepository = UserRepositoryImpl.create(databaseManager);
            experienceRepository = ExperienceRepositoryImpl.create(databaseManager);
        }
        catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        FilterService filterService = new FilterService()
                .registerFilter(new RenovateForcedPushFilter());

        CommandClientBuilder builder = new CommandClientBuilder()
                // slash commands registry
                .addSlashCommands(
                        new AvatarCommand(config),
                        new BanCommand(config),
                        new BotInfoCommand(config),
                        new ClearCommand(config),
                        new CooldownCommand(config),
                        new EmbedCommand(),
                        new KickCommand(config),
                        new PingCommand(config),
                        new ServerCommand(config),
                        new MinecraftServerInfoCommand(),
                        new SayCommand())
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId)
                .setActivity(Activity.playing("IntelliJ IDEA"))
                .useHelpBuilder(false);
        CommandClient commandClient = builder.build();

        JDA jda = JDABuilder.createDefault(getToken())
                .addEventListeners(
                        commandClient,
                        new ExperienceListener(experienceRepository),
                        new FilterMessageEmbedController(filterService)
                )
                .enableIntents(
                        EnumSet.noneOf(GatewayIntent.class)
                )
                .setAutoReconnect(true)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES) // Because JDA doesn't understand that a few lines above all intents are enabled
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .setChunkingFilter(ChunkingFilter.ALL)

                .build()
                .awaitReady();

                GuildStatisticsService guildStatisticsService = new GuildStatisticsService(config, jda);
                Timer timer = new Timer();
                timer.schedule(new GuildStatisticsTask(guildStatisticsService), 0, Duration.ofMinutes(5L).toMillis());
    }

    public static String getToken() {
        if (!IS_DEVELOPER_MODE) {
            return config.token;
        }

        return System.getenv("OFFICER_TOKEN");
    }
}