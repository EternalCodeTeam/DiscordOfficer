package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class PingCommand extends SlashCommand {

    private final AppConfig appConfig;

    public PingCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "ping";
        this.help = "Performs a ping to see the bot's delay";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        MessageEmbed build = new EmbedBuilder()
                .setTitle("🏓 | Pong!")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                .addField("Gateway Ping", gatewayPing + "ms", false)
                .addField("Rest Ping", restPing + "ms", false)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
