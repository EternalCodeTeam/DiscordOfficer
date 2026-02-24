package com.eternalcode.discordapp;

import com.eternalcode.discordapp.feature.automessages.AutoMessageService;
import com.eternalcode.discordapp.feature.automessages.AutoMessageTask;
import com.eternalcode.discordapp.feature.command.AvatarCommand;
import com.eternalcode.discordapp.feature.command.BanCommand;
import com.eternalcode.discordapp.feature.command.BotInfoCommand;
import com.eternalcode.discordapp.feature.command.ClearCommand;
import com.eternalcode.discordapp.feature.command.CooldownCommand;
import com.eternalcode.discordapp.feature.command.EmbedCommand;
import com.eternalcode.discordapp.feature.command.KickCommand;
import com.eternalcode.discordapp.feature.command.MinecraftServerInfoCommand;
import com.eternalcode.discordapp.feature.command.PingCommand;
import com.eternalcode.discordapp.feature.command.SayCommand;
import com.eternalcode.discordapp.feature.command.ServerCommand;
import com.eternalcode.discordapp.feature.command.XFixCommand;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.config.DatabaseConfig;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.feature.filter.FilterMessageEmbedController;
import com.eternalcode.discordapp.feature.filter.FilterService;
import com.eternalcode.discordapp.feature.filter.renovate.RenovateForcedPushFilter;
import com.eternalcode.discordapp.feature.guildstats.GuildStatisticsService;
import com.eternalcode.discordapp.feature.guildstats.GuildStatisticsTask;
import com.eternalcode.discordapp.feature.leveling.LevelCommand;
import com.eternalcode.discordapp.feature.leveling.LevelConfig;
import com.eternalcode.discordapp.feature.leveling.LevelController;
import com.eternalcode.discordapp.feature.leveling.LevelService;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceChangeEvent;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceConfig;
import com.eternalcode.discordapp.feature.leveling.experience.ExperienceService;
import com.eternalcode.discordapp.feature.leveling.experience.listener.ExperienceMessageListener;
import com.eternalcode.discordapp.feature.leveling.experience.listener.ExperienceReactionListener;
import com.eternalcode.discordapp.feature.leveling.leaderboard.LeaderboardButtonController;
import com.eternalcode.discordapp.feature.leveling.leaderboard.LeaderboardCommand;
import com.eternalcode.discordapp.feature.leveling.leaderboard.LeaderboardService;
import com.eternalcode.discordapp.feature.meeting.MeetingButtonController;
import com.eternalcode.discordapp.feature.meeting.MeetingCleanupTask;
import com.eternalcode.discordapp.feature.meeting.MeetingCommand;
import com.eternalcode.discordapp.feature.meeting.MeetingPollRepository;
import com.eternalcode.discordapp.feature.meeting.MeetingService;
import com.eternalcode.discordapp.feature.meeting.MeetingVoteRepository;
import com.eternalcode.discordapp.observer.ObserverRegistry;
import com.eternalcode.discordapp.feature.review.GitHubReviewReminderService;
import com.eternalcode.discordapp.feature.review.GitHubReviewService;
import com.eternalcode.discordapp.feature.review.GitHubReviewTask;
import com.eternalcode.discordapp.feature.review.command.GitHubReviewCommand;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.feature.review.database.GitHubReviewMentionRepositoryImpl;
import com.eternalcode.commons.scheduler.loom.LoomScheduler;
import com.eternalcode.commons.scheduler.loom.LoomSchedulerImpl;
import com.eternalcode.commons.scheduler.loom.MainThreadDispatcher;
import com.eternalcode.discordapp.feature.ticket.TicketConfigurer;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.sentry.Sentry;
import java.io.File;
import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private static final Duration REMINDER_INTERVAL = Duration.ofHours(24);
    private LoomScheduler scheduler;
    private GitHubReviewReminderService reminderService;
    private JDA jda;
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        new DiscordApp().start();
    }

    private void start() {
        try {
            LOGGER.info("Starting Discord Application...");

            runApplication();

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            LOGGER.info("Discord Application started successfully!");
        }
        catch (Exception exception) {
            LOGGER.error("Failed to start Discord Application", exception);
            Sentry.captureException(exception);
            throw new IllegalStateException("Discord Application startup failed", exception);
        }
    }

    private void runApplication() throws Exception {
        LOGGER.info("Loading configurations...");
        ConfigManager configManager = new ConfigManager("config");
        AppConfig appConfig = configManager.load(new AppConfig());
        DatabaseConfig databaseConfig = configManager.load(new DatabaseConfig());
        ExperienceConfig experienceConfig = configManager.load(new ExperienceConfig());
        LevelConfig levelConfig = configManager.load(new LevelConfig());

        if (!appConfig.sentryDsn.isBlank()) {
            Sentry.init(options -> {
                options.setDsn(appConfig.sentryDsn);
                options.setTracesSampleRate(1.0);
                options.setDebug(true);
                options.setAttachStacktrace(true);
            });
            LOGGER.info("Sentry initialized");
        }

        LOGGER.info("Initializing core components...");
        OkHttpClient httpClient = new OkHttpClient();
        LoomScheduler scheduler = new LoomSchedulerImpl(MainThreadDispatcher.synchronous());
        DatabaseManager databaseManager = new DatabaseManager(databaseConfig, new File("database"));
        databaseManager.connect();
        ObserverRegistry observerRegistry = new ObserverRegistry();

        LOGGER.info("Initializing repositories...");
        GitHubReviewMentionRepository mentionRepo =
            GitHubReviewMentionRepositoryImpl.create(databaseManager, scheduler);

        LOGGER.info("Initializing services...");
        ExperienceService experienceService = new ExperienceService(databaseManager, observerRegistry);
        LevelService levelService = new LevelService(databaseManager);
        GitHubReviewService reviewService = new GitHubReviewService(appConfig, configManager, mentionRepo);
        LeaderboardService leaderboardService = new LeaderboardService(levelService);

        MeetingPollRepository meetingPollRepository = MeetingPollRepository.create(databaseManager);
        MeetingVoteRepository meetingVoteRepository = MeetingVoteRepository.create(databaseManager);
        MeetingService meetingService = new MeetingService(appConfig, meetingPollRepository, meetingVoteRepository);

        LOGGER.info("Building command client...");
        CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
            .setOwnerId(appConfig.topOwnerId)
            .setActivity(Activity.playing("IntelliJ IDEA"))
            .useHelpBuilder(false)
            .forceGuildOnly(appConfig.guildId)
            .addSlashCommands(
                new AvatarCommand(appConfig),
                new BanCommand(appConfig),
                new BotInfoCommand(appConfig),
                new ClearCommand(appConfig),
                new CooldownCommand(appConfig),
                new EmbedCommand(),
                new KickCommand(appConfig),
                new MinecraftServerInfoCommand(httpClient),
                new PingCommand(appConfig),
                new SayCommand(),
                new ServerCommand(appConfig),
                new XFixCommand(),
                new GitHubReviewCommand(reviewService, appConfig),
                new LevelCommand(levelService),
                new LeaderboardCommand(leaderboardService),
                new MeetingCommand(meetingService)
            );

        LOGGER.info("Initializing Discord bot...");
        FilterService filterService = new FilterService().register(new RenovateForcedPushFilter());
        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        JDA jda = JDABuilder.createDefault(appConfig.token)
            .setEventPool(virtualThreadExecutor, true)
            .setCallbackPool(virtualThreadExecutor, true)
            .addEventListeners(
                new ExperienceMessageListener(experienceConfig, experienceService),
                new ExperienceReactionListener(experienceConfig, experienceService),
                new FilterMessageEmbedController(filterService),
                new LeaderboardButtonController(leaderboardService),
                new MeetingButtonController(meetingService)
            )
            .setAutoReconnect(true)
            .setHttpClient(httpClient)
            .enableIntents(EnumSet.allOf(GatewayIntent.class))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableCache(CacheFlag.ONLINE_STATUS)
            .setChunkingFilter(ChunkingFilter.ALL)
            .build()
            .awaitReady();

        observerRegistry.observe(
            ExperienceChangeEvent.class,
            new LevelController(levelConfig, levelService, jda)
        );

        LOGGER.info("Initializing JDA-dependent services...");
        GuildStatisticsService guildStats = new GuildStatisticsService(appConfig, jda);
        AutoMessageService autoMsgService = new AutoMessageService(jda, appConfig.autoMessagesConfig);

        TicketConfigurer ticketConfigurer = new TicketConfigurer(
            jda,
            configManager,
            databaseManager,
            scheduler,
            commandClientBuilder,
            appConfig
        );
        ticketConfigurer.initialize();

        CommandClient commandClient = commandClientBuilder.build();
        LOGGER.info("CommandClient built and registered");
        jda.addEventListener(commandClient);

        GitHubReviewReminderService reminderService = new GitHubReviewReminderService(
            jda,
            mentionRepo,
            appConfig,
            REMINDER_INTERVAL
        );
        reminderService.start();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Sentry.captureException(throwable);
            LOGGER.error("Uncaught exception in thread: {}", thread.getName(), throwable);
        });

        LOGGER.info("Starting scheduled tasks...");
        scheduler.runAsyncLater(new GuildStatisticsTask(guildStats), Duration.ofMinutes(5));
        new GitHubReviewTask(reviewService, jda, scheduler).start();
        scheduler.runAsyncTimer(new AutoMessageTask(autoMsgService), Duration.ZERO, appConfig.autoMessagesConfig.interval);

        LOGGER.info("Auto messages scheduled with interval: {}", appConfig.autoMessagesConfig.interval);

        scheduler.runAsyncTimer(new MeetingCleanupTask(meetingService, jda), Duration.ZERO, Duration.ofHours(1));

        this.scheduler = scheduler;
        this.reminderService = reminderService;
        this.jda = jda;
        this.databaseManager = databaseManager;
    }

    private void shutdown() {
        LOGGER.info("Initiating graceful shutdown...");

        try {
            if (scheduler != null) {
                scheduler.shutdown(Duration.ofSeconds(30));
                LOGGER.info("Scheduler stopped");
            }

            if (reminderService != null) {
                reminderService.stop();
                LOGGER.info("GitHub review reminder service stopped");
            }

            if (jda != null) {
                jda.shutdown();
                LOGGER.info("JDA stopped");
            }

            if (databaseManager != null) {
                databaseManager.close();
                LOGGER.info("Database connections closed");
            }

            LOGGER.info("Graceful shutdown completed");
        }
        catch (Exception exception) {
            LOGGER.error("Error during shutdown", exception);
            Sentry.captureException(exception);
        }
    }
}

