package com.eternalcode.discordapp;

import com.eternalcode.discordapp.automessages.AutoMessageService;
import com.eternalcode.discordapp.automessages.AutoMessageTask;
import com.eternalcode.discordapp.command.AvatarCommand;
import com.eternalcode.discordapp.command.BanCommand;
import com.eternalcode.discordapp.command.BotInfoCommand;
import com.eternalcode.discordapp.command.ClearCommand;
import com.eternalcode.discordapp.command.CooldownCommand;
import com.eternalcode.discordapp.command.EmbedCommand;
import com.eternalcode.discordapp.command.KickCommand;
import com.eternalcode.discordapp.command.MinecraftServerInfoCommand;
import com.eternalcode.discordapp.command.PingCommand;
import com.eternalcode.discordapp.command.SayCommand;
import com.eternalcode.discordapp.command.ServerCommand;
import com.eternalcode.discordapp.command.XFixCommand;
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
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceMessageListener;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceReactionListener;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardButtonController;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardCommand;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardService;
import com.eternalcode.discordapp.observer.ObserverRegistry;
import com.eternalcode.discordapp.review.GitHubReviewReminderService;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.GitHubReviewTask;
import com.eternalcode.discordapp.review.command.GitHubReviewCommand;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepositoryImpl;
import com.eternalcode.discordapp.scheduler.Scheduler;
import com.eternalcode.discordapp.scheduler.VirtualThreadSchedulerImpl;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.sentry.Sentry;
import java.io.File;
import java.time.Duration;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordApp.class);
    private static final Duration REMINDER_INTERVAL = Duration.ofMinutes(2);

    private final ApplicationComponents components = new ApplicationComponents();

    public static void main(String... args) {
        new DiscordApp().start();
    }

    private void start() {
        try {
            LOGGER.info("Starting Discord Application...");

            // Initialize everything
            initializeApplication();

            // Setup graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

            LOGGER.info("Discord Application started successfully!");

            // Keep application running
            Thread.currentThread().join();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to start Discord Application", exception);
            Sentry.captureException(exception);
            System.exit(1);
        }
    }

    private void initializeApplication() throws Exception {
        // Load configurations
        LOGGER.info("Loading configurations...");
        components.configManager = new ConfigManager("config");
        components.appConfig = components.configManager.load(new AppConfig());
        components.databaseConfig = components.configManager.load(new DatabaseConfig());
        components.experienceConfig = components.configManager.load(new ExperienceConfig());
        components.levelConfig = components.configManager.load(new LevelConfig());

        // Initialize Sentry
        if (!components.appConfig.sentryDsn.isEmpty()) {
            Sentry.init(options -> {
                options.setDsn(components.appConfig.sentryDsn);
                options.setTracesSampleRate(1.0);
                options.setDebug(true);
                options.setAttachStacktrace(true);
            });
            LOGGER.info("Sentry initialized");
        }

        // Initialize core components
        LOGGER.info("Initializing core components...");
        components.databaseManager = new DatabaseManager(components.databaseConfig, new File("database"));
        components.databaseManager.connect();
        components.observerRegistry = new ObserverRegistry();
        components.httpClient = new OkHttpClient();
        components.scheduler = new VirtualThreadSchedulerImpl();

        // Initialize repositories
        LOGGER.info("Initializing repositories...");
        components.mentionRepository =
            GitHubReviewMentionRepositoryImpl.create(components.databaseManager, components.scheduler);

        // Initialize services
        LOGGER.info("Initializing services...");
        components.experienceService = new ExperienceService(components.databaseManager, components.observerRegistry);
        components.levelService = new LevelService(components.databaseManager);
        components.gitHubReviewService = new GitHubReviewService(
            components.appConfig,
            components.configManager,
            components.mentionRepository
        );
        components.leaderboardService = new LeaderboardService(components.levelService);

        // Build command client
        LOGGER.info("Building command client...");
        CommandClient commandClient = new CommandClientBuilder()
            .setOwnerId(components.appConfig.topOwnerId)
            .setActivity(Activity.playing("IntelliJ IDEA"))
            .useHelpBuilder(false)
            .addSlashCommands(
                // Standard commands
                new AvatarCommand(components.appConfig),
                new BanCommand(components.appConfig),
                new BotInfoCommand(components.appConfig),
                new ClearCommand(components.appConfig),
                new CooldownCommand(components.appConfig),
                new EmbedCommand(),
                new KickCommand(components.appConfig),
                new MinecraftServerInfoCommand(components.httpClient),
                new PingCommand(components.appConfig),
                new SayCommand(),
                new ServerCommand(components.appConfig),
                new XFixCommand(),

                // Feature commands
                new GitHubReviewCommand(components.gitHubReviewService, components.appConfig),
                new LevelCommand(components.levelService),
                new LeaderboardCommand(components.leaderboardService)
            )
            .build();

        FilterService filterService = new FilterService()
            .register(new RenovateForcedPushFilter());

        LOGGER.info("Initializing Discord bot...");
        components.jda = JDABuilder.createDefault(components.appConfig.token)
            .addEventListeners(
                commandClient,
                new ExperienceMessageListener(components.experienceConfig, components.experienceService),
                new ExperienceReactionListener(components.experienceConfig, components.experienceService),
                new FilterMessageEmbedController(filterService),
                new LeaderboardButtonController(components.leaderboardService)
            )
            .setAutoReconnect(true)
            .setHttpClient(components.httpClient)
            .enableIntents(EnumSet.allOf(GatewayIntent.class))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(CacheFlag.ONLINE_STATUS)
            .setChunkingFilter(ChunkingFilter.ALL)
            .build()
            .awaitReady();

        components.observerRegistry.observe(
            ExperienceChangeEvent.class,
            new LevelController(components.levelConfig, components.levelService, components.jda)
        );

        LOGGER.info("Initializing JDA-dependent services...");
        components.guildStatisticsService = new GuildStatisticsService(components.appConfig, components.jda);
        components.autoMessageService = new AutoMessageService(components.jda, components.appConfig.autoMessagesConfig);
        components.gitHubReviewReminderService = new GitHubReviewReminderService(
            components.jda,
            components.mentionRepository,
            components.appConfig,
            components.scheduler,
            REMINDER_INTERVAL
        );
        components.gitHubReviewReminderService.start();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Sentry.captureException(throwable);
            LOGGER.error("Uncaught exception in thread: {}", thread.getName(), throwable);
        });

        LOGGER.info("Starting scheduled tasks...");
        components.scheduler.schedule(
            new GuildStatisticsTask(components.guildStatisticsService),
            Duration.ofMinutes(5)
        );

        new GitHubReviewTask(components.gitHubReviewService, components.jda, components.scheduler).start();

        components.scheduler.scheduleRepeating(
            new AutoMessageTask(components.autoMessageService),
            components.appConfig.autoMessagesConfig.interval
        );

        LOGGER.info("Auto messages scheduled with interval: {}", components.appConfig.autoMessagesConfig.interval);
    }

    private void shutdown() {
        LOGGER.info("Initiating graceful shutdown...");

        try {
            if (components.scheduler != null) {
                try {
                    components.scheduler.shutdown();
                    LOGGER.info("Scheduler stopped");
                }
                catch (InterruptedException exception) {
                    LOGGER.error("Scheduler shutdown was interrupted", exception);
                    Thread.currentThread().interrupt();
                }
            }

            if (components.gitHubReviewReminderService != null) {
                components.gitHubReviewReminderService.stop();
                LOGGER.info("GitHub review reminder service stopped");
            }

            if (components.jda != null) {
                components.jda.shutdown();
                LOGGER.info("JDA stopped");
            }

            if (components.databaseManager != null) {
                components.databaseManager.close();
                LOGGER.info("Database connections closed");
            }

            LOGGER.info("Graceful shutdown completed");
        }
        catch (Exception exception) {
            LOGGER.error("Error during shutdown", exception);
            Sentry.captureException(exception);
        }
    }

    private static class ApplicationComponents {
        // Configurations
        ConfigManager configManager;
        AppConfig appConfig;
        DatabaseConfig databaseConfig;
        ExperienceConfig experienceConfig;
        LevelConfig levelConfig;

        // Core components
        DatabaseManager databaseManager;
        ObserverRegistry observerRegistry;
        Scheduler scheduler;
        JDA jda;
        OkHttpClient httpClient;

        // Services
        ExperienceService experienceService;
        LevelService levelService;
        GitHubReviewService gitHubReviewService;
        GitHubReviewReminderService gitHubReviewReminderService;
        LeaderboardService leaderboardService;
        GuildStatisticsService guildStatisticsService;
        AutoMessageService autoMessageService;
        GitHubReviewMentionRepository mentionRepository;
    }
}
