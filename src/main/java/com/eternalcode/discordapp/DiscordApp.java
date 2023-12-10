package com.eternalcode.discordapp;

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
import com.eternalcode.discordapp.leveling.experience.voice.ExperienceJoinVoiceController;
import com.eternalcode.discordapp.leveling.experience.voice.ExperienceLeaveVoiceController;
import com.eternalcode.discordapp.leveling.experience.voice.ExperienceVoiceActivityData;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceMessageListener;
import com.eternalcode.discordapp.leveling.experience.listener.ExperienceReactionListener;
import com.eternalcode.discordapp.leveling.games.CodeGameAnswerController;
import com.eternalcode.discordapp.leveling.games.CodeGameConfiguration;
import com.eternalcode.discordapp.leveling.games.CodeImageGameData;
import com.eternalcode.discordapp.leveling.games.GenerateImageWithCodeTask;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardButtonController;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardCommand;
import com.eternalcode.discordapp.leveling.leaderboard.LeaderboardService;
import com.eternalcode.discordapp.meeting.MeetingController;
import com.eternalcode.discordapp.meeting.MeetingService;
import com.eternalcode.discordapp.meeting.command.MeetingCommand;
import com.eternalcode.discordapp.meeting.event.MeetingCreateEvent;
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
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Timer;

public class DiscordApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordApp.class);

    private static ExperienceService experienceService;
    private static LevelService levelService;
    private static MeetingService meetingService;

    public static void main(String... args) throws InterruptedException {
        ObserverRegistry observerRegistry = new ObserverRegistry();
        ConfigManager configManager = new ConfigManager("config");

        AppConfig config = configManager.load(new AppConfig());
        DatabaseConfig databaseConfig = configManager.load(new DatabaseConfig());
        ExperienceConfig experienceConfig = configManager.load(new ExperienceConfig());
        LevelConfig levelConfig = configManager.load(new LevelConfig());
        CodeGameConfiguration codeGameConfiguration = configManager.load(new CodeGameConfiguration());

        ConfigManager data = new ConfigManager("data");
        ExperienceVoiceActivityData experienceVoiceActivityData = data.load(new ExperienceVoiceActivityData());
        CodeImageGameData codeImageGameData = data.load(new CodeImageGameData());

        data.save(experienceVoiceActivityData);

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
            meetingService = new MeetingService(databaseManager, observerRegistry);
        }
        catch (SQLException exception) {
            Sentry.captureException(exception);
            LOGGER.error("Failed to connect to database", exception);
        }

        LeaderboardService leaderboardService = new LeaderboardService(levelService);

        OkHttpClient httpClient = new OkHttpClient();

        FilterService filterService = new FilterService()
            .registerFilter(new RenovateForcedPushFilter());

        GitHubReviewService gitHubReviewService = new GitHubReviewService(config, configManager);

        CommandClient commandClient = new CommandClientBuilder()
            .setOwnerId(config.topOwnerId)
            .setActivity(Activity.playing("IntelliJ IDEA"))
            .useHelpBuilder(false)

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
                new MinecraftServerInfoCommand(httpClient),
                new PingCommand(config),
                new SayCommand(),
                new ServerCommand(config),

                // GitHub review
                new GitHubReviewCommand(gitHubReviewService, config),

                // Leveling
                new LevelCommand(levelService),
                new LeaderboardCommand(leaderboardService),

                // meeting
                new MeetingCommand(config, meetingService)
            )
            .build();

        JDA jda = JDABuilder.createDefault(config.token)
            .addEventListeners(
                // Slash commands
                commandClient,

                // Experience system
                new ExperienceMessageListener(experienceConfig, experienceService),
                new ExperienceReactionListener(experienceConfig, experienceService),

                // experience voice
                new ExperienceLeaveVoiceController(experienceVoiceActivityData, data, experienceConfig, experienceService),
                new ExperienceJoinVoiceController(experienceVoiceActivityData, data),

                // Message filter
                new FilterMessageEmbedController(filterService),

                // Experience games
                new CodeGameAnswerController(codeImageGameData, codeGameConfiguration, data, experienceService),

                // leaderboard
                new LeaderboardButtonController(leaderboardService)
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
        observerRegistry.observe(MeetingCreateEvent.class, new MeetingController(jda, config));

        GuildStatisticsService guildStatisticsService = new GuildStatisticsService(config, jda);

        Timer timer = new Timer();
        timer.schedule(new GuildStatisticsTask(guildStatisticsService), 0, Duration.ofMinutes(5L).toMillis());
        timer.schedule(new GitHubReviewTask(gitHubReviewService, jda), 0, Duration.ofMinutes(15L).toMillis());
        timer.schedule(new GenerateImageWithCodeTask(codeImageGameData, codeGameConfiguration, jda, data), 0, Duration.ofMinutes(codeGameConfiguration.timeToNextQuestion).toMillis());
    }
}
