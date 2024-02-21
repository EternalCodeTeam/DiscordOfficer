package com.eternalcode.discordapp.ticket;

import com.eternalcode.discordapp.config.AppConfig;
import net.dv8tion.jda.api.EmbedBuilder;

import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Objects;

public class TicketButtonController extends ListenerAdapter {

    AppConfig appConfig;

    public TicketButtonController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("create_ticket")) {
            MessageChannel channel = event.getChannel();
            Category category = Objects.requireNonNull(event.getGuild()).getCategoryById(this.appConfig.ticketSystem.ticketCategoryId);

            if (category != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Błąd")
                    .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                    .setDescription("Kategoria nie jest podana w configu.");
                event.replyEmbeds(embedBuilder.build());
            }

            if (!event.getGuild().getTextChannelsByName(event.getMember().getId(), false).isEmpty()) {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Błąd")
                    .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                    .setDescription("Posiadasz już otwarty ticket.");
                event.replyEmbeds(embedBuilder.build());
            }

            category.createTextChannel(event.getMember().getId())
                .flatMap(ticket -> channel.sendMessageFormat("Twój ticket jest tutaj ---> %s", ticket))
                .queue();
        }
    }
}

