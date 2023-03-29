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
import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.eternalcode.discordapp.config.DiscordAppConfigManager;
import com.eternalcode.discordapp.filter.FilterMessageEmbedController;
import com.eternalcode.discordapp.filter.FilterService;
import com.eternalcode.discordapp.filter.renovate.RenovateForcedPushFilter;
import com.eternalcode.discordapp.guildstats.GuildStatisticsService;
import com.eternalcode.discordapp.guildstats.GuildStatisticsTask;
import com.eternalcode.discordapp.review.GitHubReviewCommand;
import com.eternalcode.discordapp.review.GitHubReviewService;
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
import java.io.IOException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Timer;

public class DiscordApp {

    public static void main(String... args) throws InterruptedException, IOException {
        DiscordAppConfigManager configManager = new DiscordAppConfigManager(new File("config"));
        DiscordAppConfig config = new DiscordAppConfig();
        configManager.load(config);

        OkHttpClient httpClient = new OkHttpClient();

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
                        new MinecraftServerInfoCommand(httpClient),
                        new SayCommand()
                )
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId)
                .setActivity(Activity.playing("IntelliJ IDEA"))
                .useHelpBuilder(false);
        CommandClient commandClient = builder.build();

        JDA jda = JDABuilder.createDefault(config.token)
                .addEventListeners(
                        // commands
                        commandClient,

                        // filters
                        new FilterMessageEmbedController(filterService)
                )

                .setAutoReconnect(true)

                // enable all intents
                .enableIntents(EnumSet.noneOf(GatewayIntent.class))

                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES) // Because JDA doesn't understand that a few lines above all intents are enabled
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .setChunkingFilter(ChunkingFilter.ALL)

                .build()
                .awaitReady();

        GuildStatisticsService guildStatisticsService = new GuildStatisticsService(config, jda);
        GitHubReviewService gitHubReviewService = new GitHubReviewService(config, jda);
        gitHubReviewService.automaticCreatePullRequests();

        Timer timer = new Timer();
        timer.schedule(new GuildStatisticsTask(guildStatisticsService), 0, Duration.ofMinutes(5L).toMillis());

/*        // ONLY FOR TESTING
        timer.schedule(new GitHubReviewTask(jda, httpClient, config), 0, Duration.ofSeconds(10L).toMillis());*/
    }
}