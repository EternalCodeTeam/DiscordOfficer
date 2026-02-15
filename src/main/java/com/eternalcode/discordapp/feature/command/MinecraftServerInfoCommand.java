package com.eternalcode.discordapp.feature.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class MinecraftServerInfoCommand extends SlashCommand {

    private static final String API_URL = "https://api.mcsrvstat.us/2/%s";
    private static final String IMAGE_API_URL = "https://api.mcsrvstat.us/icon/%s";

    private static final Gson GSON = new Gson();

    private final OkHttpClient httpClient;

    public MinecraftServerInfoCommand(OkHttpClient httpClient) {
        this.name = "minecraft";
        this.aliases = new String[]{ "mc", "mcserver" };
        this.options = List.of(
                new OptionData(OptionType.STRING, "address", "The domain/IP address of the Minecraft server")
                        .setRequired(true)
        );

        this.httpClient = httpClient;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String domain = event.getOption("address").getAsString();

        String request = this.sendApiRequest(domain);

        if (request == null) {
            event.reply("❌ An error occurred while fetching the server info!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        JsonObject response = JsonParser.parseString(request).getAsJsonObject();

        if (!response.get("online").getAsBoolean()) {
            event.reply("❌ The provided server is offline!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        JsonObject players = response.get("players").getAsJsonObject();
        int onlinePlayers = players.get("online").getAsInt();
        int maxPlayers = players.get("max").getAsInt();

        String version = response.get("version").getAsString();
        String ip = response.get("ip").getAsString();

        JsonObject motdObject = response.get("motd").getAsJsonObject();
        JsonArray motdClean = motdObject.get("clean").getAsJsonArray();

        String[] motd = GSON.fromJson(motdClean, String[].class);

        MessageEmbed build = new EmbedBuilder()
                .setTitle("Minecraft server info")
                .setThumbnail(String.format(IMAGE_API_URL, domain))
                .setColor(Color.decode("#FFA500"))
                .addField("Domain", domain, true)
                .addField("Version", version, true)
                .addField("IP", ip, true)
                .addField("Players", onlinePlayers + "/" + maxPlayers, true)
                .addField("MOTD", String.join("\n", motd), false)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();

    }

    private String sendApiRequest(String serverAddress) {
        Request request = new Request.Builder()
                .url(String.format(API_URL, serverAddress))
                .get()
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return response.body().string();
        }
        catch (IOException exception) {
            Sentry.captureException(exception);
            exception.printStackTrace();
            return null;
        }
    }
}

