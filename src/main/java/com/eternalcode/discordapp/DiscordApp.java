package com.eternalcode.discordapp;

import com.freya02.botcommands.api.CommandsBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

public class DiscordApp {

    private static final boolean isDeveloperMode = true;

    public static void main(String... args) throws InterruptedException, IOException {
        JDA jda = JDABuilder.createDefault(getToken())
                .build();

        jda.awaitReady();

        long topOwnerId = 852920601969950760L; // TODO: Move to config

        CommandsBuilder commandsBuilder = CommandsBuilder.newBuilder(topOwnerId);
        commandsBuilder.build(jda, "com.eternalcode.discordapp.command");
    }

    public static String getToken() {
        if (!isDeveloperMode) {
            return "token"; // TODO: Implement configs
        }

        return System.getenv("DEVELOPER_DISCORD_TOKEN").toString();
    }

}