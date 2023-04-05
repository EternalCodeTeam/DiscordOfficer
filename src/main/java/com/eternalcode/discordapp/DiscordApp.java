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
import com.eternalcode.discordapp.experience.ExperienceConfig;
import com.eternalcode.discordapp.experience.ExperienceRepository;
import com.eternalcode.discordapp.experience.ExperienceRepositoryImpl;
import com.eternalcode.discordapp.experience.listener.ExperienceMessageListener;
import com.eternalcode.discordapp.filter.FilterMessageEmbedController;
import com.eternalcode.discordapp.filter.FilterService;
import com.eternalcode.discordapp.filter.renovate.RenovateForcedPushFilter;
import com.eternalcode.discordapp.guildstats.GuildStatisticsService;
import com.eternalcode.discordapp.guildstats.GuildStatisticsTask;
import com.eternalcode.discordapp.review.GitHubReviewCommand;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.ReviewController;
import com.eternalcode.discordapp.review.ReviewRepository;
import com.eternalcode.discordapp.review.ReviewRepositoryImpl;
import com.eternalcode.discordapp.user.UserRepositoryImpl;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Timer;

public class DiscordApp {

    private static ExperienceRepository experienceRepository;
    private static ReviewRepository reviewRepository;

    public static void main(String... args) throws InterruptedException {
        ConfigManager configManager = new ConfigManager(new File("config"));

        AppConfig config = new AppConfig();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        ExperienceConfig experienceConfig = new ExperienceConfig();

        configManager.load(config);
        configManager.load(databaseConfig);
        configManager.load(experienceConfig);

        try {
            DatabaseManager databaseManager = new DatabaseManager(databaseConfig, new File("database"));
            databaseManager.connect();
            UserRepositoryImpl.create(databaseManager);

            experienceRepository = ExperienceRepositoryImpl.create(databaseManager);
            reviewRepository = ReviewRepositoryImpl.create(databaseManager);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }

        OkHttpClient httpClient = new OkHttpClient();

        FilterService filterService = new FilterService()
                .registerFilter(new RenovateForcedPushFilter());

        CommandClient commandClient = new CommandClientBuilder()
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
                        new MinecraftServerInfoCommand(httpClient),
                        new SayCommand(),
                        new GitHubReviewCommand(new GitHubReviewService(config, reviewRepository))
                )
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId)
                .setActivity(Activity.playing("IntelliJ IDEA"))
                .useHelpBuilder(false)
                .build();

        JDA jda = JDABuilder.createDefault(config.token)
                .addEventListeners(
                        // Slash commands
                        commandClient,

                        // Experience system
                        new ExperienceMessageListener(experienceRepository, experienceConfig),

                        // Message filter
                        new FilterMessageEmbedController(filterService),

                        new ReviewController(reviewRepository)
                )

                .setAutoReconnect(true)

                .enableIntents(EnumSet.noneOf(GatewayIntent.class))

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
}