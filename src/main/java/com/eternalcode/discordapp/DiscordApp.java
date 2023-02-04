package com.eternalcode.discordapp;

import com.eternalcode.discordapp.command.AvatarCommand;
import com.eternalcode.discordapp.command.BanCommand;
import com.eternalcode.discordapp.command.BotInfoCommand;
import com.eternalcode.discordapp.command.ClearCommand;
import com.eternalcode.discordapp.command.CooldownCommand;
import com.eternalcode.discordapp.command.KickCommand;
import com.eternalcode.discordapp.command.PingCommand;
import com.eternalcode.discordapp.command.ServerCommand;
import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.eternalcode.discordapp.config.DiscordAppConfigManager;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;

public class DiscordApp {

    private static final boolean IS_DEVELOPER_MODE = true;
    private static DiscordAppConfig config;

    public static void main(String... args) {
        DiscordAppConfigManager configManager = new DiscordAppConfigManager(new File("config"));
        config = new DiscordAppConfig();
        configManager.load(config);

        CommandClientBuilder builder = new CommandClientBuilder()
                .addSlashCommand(new AvatarCommand())
                .addSlashCommand(new PingCommand())
                .addSlashCommand(new ServerCommand(config))
                .addSlashCommand(new KickCommand(config))
                .addSlashCommand(new CooldownCommand(config))
                .addSlashCommand(new ClearCommand(config))
                .addSlashCommand(new BotInfoCommand(config))
                .addSlashCommand(new BanCommand(config))
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId);
        CommandClient commandClient = builder.build();

        JDABuilder.createDefault(getToken())
                .addEventListeners(commandClient)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
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