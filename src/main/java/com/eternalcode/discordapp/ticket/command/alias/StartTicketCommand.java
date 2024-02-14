package com.eternalcode.discordapp.ticket.command.alias;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class StartTicketCommand extends SlashCommand {
    private final AppConfig appConfig;

    public StartTicketCommand(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.name = "start";
        this.help = "Starter ticket command";

        this.userPermissions = new Permission[]{Permission.ADMINISTRATOR};

        this.options = List.of(
            new OptionData(OptionType.CHANNEL, "channel",
                "channel to which the message is to be sent")
                .setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        long optionChannelId = event.getOption("channel").getAsChannel().getIdLong();
        MessageChannel optionChannel = event.getGuild().getTextChannelById(optionChannelId);

        if (optionChannel != null) {
            MessageEmbed ticketMessage = new EmbedBuilder()
                .setTitle("Ticket")
                .setColor(Color.decode(appConfig.embedSettings.successEmbed.color))
                .setThumbnail(appConfig.embedSettings.successEmbed.thumbnail)
                .setTimestamp(Instant.now())
                .build();

            Button firstButton = Button.success("create_ticket", "Otwórz bilet");

            optionChannel.sendMessageEmbeds(ticketMessage)
                .setActionRow(firstButton)
                .queue();

            MessageEmbed build = new EmbedBuilder()
                .setTitle("Wiadomośc została wysłana")
                .setColor(Color.decode(appConfig.embedSettings.successEmbed.color))
                .setThumbnail(appConfig.embedSettings.successEmbed.thumbnail)
                .setTimestamp(Instant.now())
                .build();

            event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
        } else {
            MessageEmbed errorMessage = new EmbedBuilder()
                .setTitle("Błąd")
                .setColor(Color.decode(appConfig.embedSettings.errorEmbed.color))
                .setThumbnail(appConfig.embedSettings.errorEmbed.thumbnail)
                .setTimestamp(Instant.now())
                .setDescription("Podany kanał nie jest kanałem tekstowym")
                .build();

            event.replyEmbeds(errorMessage)
                .setEphemeral(true)
                .queue();
        }
    }
}
