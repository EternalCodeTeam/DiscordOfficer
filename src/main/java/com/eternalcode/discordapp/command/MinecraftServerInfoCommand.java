package com.eternalcode.discordapp.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class MinecraftServerInfoCommand extends SlashCommand {

    private static final String API_URL = "https://api.mcsrvstat.us/2/";
    private static final String IMAGE_API_URL = "https://api.mcsrvstat.us/icon/%s";

    private final OkHttpClient client = new OkHttpClient();

    public MinecraftServerInfoCommand() {
        this.name = "minecraft";
        this.aliases = new String[]{ "mc", "mcserver" };
        this.options = List.of(
                new OptionData(OptionType.STRING, "domain", "The domain of the Minecraft server")
                        .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String domain = event.getOption("domain").getAsString();

        String request = this.sendApiRequest(domain);

        JsonObject response = JsonParser.parseString(request).getAsJsonObject();

        if (!response.get("online").getAsBoolean()) {
            event.reply("❌ The server is offline!")
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
        String motdCleanLine1 = motdClean.get(0).getAsString();
        String motdCleanLine2 = motdClean.get(1).getAsString();

        MessageEmbed build = new EmbedBuilder()
                .setTitle("Minecraft server info")
                .setThumbnail(String.format(IMAGE_API_URL, domain))
                .setColor(Color.decode("#FFA500"))
                .addField("Domain", domain, true)
                .addField("Version", version, true)
                .addField("IP", ip, true)
                .addField("Players", onlinePlayers + "/" + maxPlayers, true)
                .addField("MOTD", motdCleanLine1 + "\n" + motdCleanLine2, false)
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();

    }

    private String sendApiRequest(String serverAddress) {
        Request request = new Request.Builder()
                .url(API_URL + serverAddress)
                .get()
                .build();

        try (Response response = this.client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}