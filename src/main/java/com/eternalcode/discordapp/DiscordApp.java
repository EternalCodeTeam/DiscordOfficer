package com.eternalcode.discordapp;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.eternalcode.discordapp.config.DiscordAppConfigManager;
import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.File;
import java.io.IOException;

public class DiscordApp {

    private static final boolean IS_DEVELOPER_MODE = true;
    private static DiscordAppConfig config;

    public static void main(String... args) throws InterruptedException, IOException {
        JDA jda = JDABuilder.createDefault(getToken())
                .build();

        jda.awaitReady();

        DiscordAppConfigManager configManager = new DiscordAppConfigManager(new File("config"));
        config = new DiscordAppConfig();
        configManager.load(config);

        CommandsBuilder commandsBuilder = CommandsBuilder.newBuilder(config.topOwnerId);
        commandsBuilder.build(jda, "com.eternalcode.discordapp.command");
    }

    public static String getToken() {
        if (!IS_DEVELOPER_MODE) {
            return config.token;
        }

        return System.getenv("DEVELOPER_DISCORD_TOKEN");
    }

}