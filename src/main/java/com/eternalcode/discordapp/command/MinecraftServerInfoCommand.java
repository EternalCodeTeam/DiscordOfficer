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

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;

public class MinecraftServerInfoCommand extends SlashCommand {

    private static final String API_URL = "https://api.mcsrvstat.us/2/";
    private static final String IMAGE_API_URL = "https://api.mcsrvstat.us/icon/%s";

    public MinecraftServerInfoCommand() {
        this.name = "minecraft";
        this.aliases = new String[]{ "mc", "mcserver" };
        this.options = List.of(
                new OptionData(OptionType.STRING, "address", "The domain/IP address of the Minecraft server")
                        .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String domain = event.getOption("domain").getAsString();


        this.sendApiRequest(domain);

        JsonObject response = JsonParser.parseString(this.sendApiRequest(domain)).getAsJsonObject();

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

    String sendApiRequest(String serverAddress) {
        try {
            URL url = new URL(API_URL + serverAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
