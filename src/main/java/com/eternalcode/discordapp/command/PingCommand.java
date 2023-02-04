package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class PingCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public PingCommand(DiscordAppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;

        this.name = "ping";
        this.help = "Performs a ping to see the bot's delay";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        MessageEmbed build = new EmbedBuilder()
                .setTitle("🏓 | Pong!")
                .addField("Gateway Ping", gatewayPing + "ms", false)
                .addField("Rest Ping", restPing + "ms", false)
                .setTimestamp(Instant.now())
                .setThumbnail(this.discordAppConfig.embedSettings.successEmbed.thumbnail)
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
