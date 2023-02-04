package com.eternalcode.discordapp;

import com.eternalcode.discordapp.command.AvatarCommand;
import com.eternalcode.discordapp.command.PingCommand;
import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.eternalcode.discordapp.config.DiscordAppConfigManager;
import com.freya02.botcommands.api.CommandsBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.File;
import java.io.IOException;

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
                .setOwnerId(config.topOwnerId)
                .forceGuildOnly(config.guildId);
        CommandClient commandClient = builder.build();

        JDABuilder.createDefault(getToken())
                .addEventListeners(commandClient)
                .build();
    }

    public static String getToken() {
        if (!IS_DEVELOPER_MODE) {
            return config.token;
        }

        return System.getenv("OFFICER_TOKEN");
    }
}