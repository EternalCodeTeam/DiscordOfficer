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
import com.eternalcode.discordapp.ticket.TicketConfig;
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
import com.eternalcode.discordapp.meeting.MeetingButtonController;
import com.eternalcode.discordapp.meeting.MeetingCleanupTask;
import com.eternalcode.discordapp.meeting.MeetingCommand;
import com.eternalcode.discordapp.meeting.MeetingPollRepository;
import com.eternalcode.discordapp.meeting.MeetingService;
import com.eternalcode.discordapp.meeting.MeetingVoteRepository;
import com.eternalcode.discordapp.observer.ObserverRegistry;
import com.eternalcode.discordapp.review.GitHubReviewReminderService;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.GitHubReviewTask;
import com.eternalcode.discordapp.review.command.GitHubReviewCommand;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepository;
import com.eternalcode.discordapp.review.database.GitHubReviewMentionRepositoryImpl;
import com.eternalcode.discordapp.scheduler.Scheduler;
import com.eternalcode.discordapp.scheduler.VirtualThreadSchedulerImpl;
import com.eternalcode.discordapp.ticket.TicketConfigurer;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.sentry.Sentry;
import java.io.File;
import java.time.Duration;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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
    private Scheduler scheduler;
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
            Thread.sleep(Long.MAX_VALUE);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to start Discord Application", exception);
            Sentry.captureException(exception);
            System.exit(1);
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
        Scheduler scheduler = new VirtualThreadSchedulerImpl();
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

        JDA jda = JDABuilder.createDefault(appConfig.token)
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
            scheduler,
            REMINDER_INTERVAL
        );
        reminderService.start();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Sentry.captureException(throwable);
            LOGGER.error("Uncaught exception in thread: {}", thread.getName(), throwable);
        });

        LOGGER.info("Starting scheduled tasks...");
        scheduler.schedule(new GuildStatisticsTask(guildStats), Duration.ofMinutes(5));
        new GitHubReviewTask(reviewService, jda, scheduler).start();
        scheduler.scheduleRepeating(new AutoMessageTask(autoMsgService), appConfig.autoMessagesConfig.interval);

        LOGGER.info("Auto messages scheduled with interval: {}", appConfig.autoMessagesConfig.interval);

        scheduler.scheduleRepeating(new MeetingCleanupTask(meetingService, jda), Duration.ofHours(1));


        this.scheduler = scheduler;
        this.reminderService = reminderService;
        this.jda = jda;
        this.databaseManager = databaseManager;
    }

    private void shutdown() {
        LOGGER.info("Initiating graceful shutdown...");

        try {
            if (scheduler != null) {
                scheduler.shutdown();
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
