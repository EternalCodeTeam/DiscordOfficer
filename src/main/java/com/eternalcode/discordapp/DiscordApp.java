package com.eternalcode.discordapp;

import com.eternalcode.discordapp.command.*;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.config.DatabaseConfig;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.filter.FilterMessageEmbedController;
import com.eternalcode.discordapp.filter.FilterService;
import com.eternalcode.discordapp.filter.renovate.RenovateForcedPushFilter;
import com.eternalcode.discordapp.guildstats.GuildStatisticsService;
import com.eternalcode.discordapp.guildstats.GuildStatisticsTask;
import com.eternalcode.discordapp.leveling.LevelCommand;
import com.eternalcode.discordapp.leveling.LevelConfig;
import com.eternalcode.discordapp.leveling.LevelController;
import com.eternalcode.discordapp.leveling.LevelService;
import com.eternalcode.discordapp.leveling.experience.ExperienceChangeEvent;
import com.eternalcode.discordapp.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.leveling.experience.ExperienceService;
import com.eternalcode.discordapp.leveling.experience.data.UsersVoiceActivityData;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceMessageListener;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceReactionListener;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceVoiceListener;
import com.eternalcode.discordapp.leveling.games.CodeImageGameData;
import com.eternalcode.discordapp.leveling.games.configuration.CodeGameConfiguration;
import com.eternalcode.discordapp.leveling.games.listener.CodeGameAnwserListener;
import com.eternalcode.discordapp.leveling.games.task.GenerateImageWithCode;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardButtonController;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardCommand;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardService;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardConfiguration;
import com.eternalcode.discordapp.observer.ObserverRegistry;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.GitHubReviewTask;
import com.eternalcode.discordapp.review.command.GitHubReviewCommand;
import com.eternalcode.discordapp.user.UserRepositoryImpl;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Timer;

public class DiscordApp {

    private static ExperienceService experienceService;
    private static LevelService levelService;

    public static void main(String... args) throws InterruptedException {
        ObserverRegistry observerRegistry = new ObserverRegistry();
        ConfigManager configManager = new ConfigManager("config");

        AppConfig config = configManager.load(new AppConfig());
        DatabaseConfig databaseConfig = configManager.load(new DatabaseConfig());
        ExperienceConfig experienceConfig = configManager.load(new ExperienceConfig());
        LevelConfig levelConfig = configManager.load(new LevelConfig());
        CodeGameConfiguration codeGameConfiguration = configManager.load(new CodeGameConfiguration());
        LeaderboardConfiguration leaderboardConfiguration = configManager.load(new LeaderboardConfiguration());

        ConfigManager data = new ConfigManager("data");
        UsersVoiceActivityData usersVoiceActivityData = data.load(new UsersVoiceActivityData());
        CodeImageGameData codeImageGameData = data.load(new CodeImageGameData());

        usersVoiceActivityData.usersOnVoiceChannel.put(0L, Instant.now());
        data.save(usersVoiceActivityData);

        if (!config.sentryDsn.isEmpty()) {
            Sentry.init(options -> {
                options.setDsn(config.sentryDsn);
                options.setTracesSampleRate(1.0);
                options.setDebug(true);
                options.setAttachStacktrace(true);
            });
        }

        try {
            DatabaseManager databaseManager = new DatabaseManager(databaseConfig, new File("database"));
            databaseManager.connect();
            UserRepositoryImpl.create(databaseManager);

            experienceService = new ExperienceService(databaseManager, observerRegistry);
            levelService = new LevelService(databaseManager);
            levelService.generateRandomLevels(25).join();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        LeaderboardService leaderboardService = new LeaderboardService(levelService);

        OkHttpClient httpClient = new OkHttpClient();

        FilterService filterService = new FilterService()
            .registerFilter(new RenovateForcedPushFilter());

        GitHubReviewService gitHubReviewService = new GitHubReviewService(config, configManager);

        CommandClient commandClient = new CommandClientBuilder()
            // slash commands registry
            .addSlashCommands(
                // Standard
                new AvatarCommand(config),
                new BanCommand(config),
                new BotInfoCommand(config),
                new ClearCommand(config),
                new CooldownCommand(config),
                new EmbedCommand(),
                new KickCommand(config),
                new PingCommand(config),
                new ServerCommand(config),
                new MinecraftServerInfoCommand(httpClient),
                new SayCommand(),

                // GitHub review
                new GitHubReviewCommand(gitHubReviewService),

                // Leveling
                new LevelCommand(levelService),
                new LeaderboardCommand(leaderboardConfiguration, leaderboardService)
            )
            .setOwnerId(config.topOwnerId)
            .setActivity(Activity.playing("IntelliJ IDEA"))
            .useHelpBuilder(false)
            .build();

        JDA jda = JDABuilder.createDefault(config.token)
            .addEventListeners(
                // Slash commands
                commandClient,

                // Experience system
                new ExperienceMessageListener(experienceConfig, experienceService),
                new ExperienceVoiceListener(experienceConfig, usersVoiceActivityData, data, experienceService),
                new ExperienceReactionListener(experienceConfig, experienceService),

                // Message filter
                new FilterMessageEmbedController(filterService),

                // Experience games
                new CodeGameAnwserListener(codeImageGameData, codeGameConfiguration, data, experienceService),

                new LeaderboardButtonController(leaderboardConfiguration, leaderboardService)
            )

            .setAutoReconnect(true)
            .setHttpClient(httpClient)

            .enableIntents(EnumSet.allOf(GatewayIntent.class))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(CacheFlag.ONLINE_STATUS)
            .setChunkingFilter(ChunkingFilter.ALL)

            .build()
            .awaitReady();

        observerRegistry.observe(ExperienceChangeEvent.class, new LevelController(levelConfig, levelService, jda));

        GuildStatisticsService guildStatisticsService = new GuildStatisticsService(config, jda);

        Timer timer = new Timer();
        timer.schedule(new GuildStatisticsTask(guildStatisticsService), 0, Duration.ofMinutes(5L).toMillis());
        timer.schedule(new GitHubReviewTask(gitHubReviewService, jda), 0, Duration.ofMinutes(15L).toMillis());
        timer.schedule(new GenerateImageWithCode(codeImageGameData, codeGameConfiguration, jda, data), 0, Duration.ofMinutes(codeGameConfiguration.timeToNextQuestion).toMillis());
    }
}
