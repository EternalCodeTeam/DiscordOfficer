package com.eternalcode.discordapp.ticket.panel;

import static com.eternalcode.discordapp.util.UrlValidator.isValid;

import com.eternalcode.discordapp.ticket.TicketChannelService;
import com.eternalcode.discordapp.ticket.TicketConfig;
import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class TicketPanelService {

    private static final int MAX_BUTTONS_PER_ROW = 5;

    private final TicketChannelService channelService;
    private final TicketConfig config;

    public TicketPanelService(TicketChannelService channelService, TicketConfig config) {
        this.channelService = channelService;
        this.config = config;
    }

    public CompletableFuture<MessageCreateData> createTicketPanel() {
        return CompletableFuture.supplyAsync(() -> {
            MessageCreateBuilder builder = new MessageCreateBuilder()
                .setEmbeds(this.createPanelEmbed());

            List<Button> buttons = config.getEnabledCategories().stream()
                .map(cat -> Button.secondary(cat.getButtonId(), cat.emoji + " " + cat.displayName))
                .collect(Collectors.toList());

            for (int i = 0; i < buttons.size(); i += MAX_BUTTONS_PER_ROW) {
                builder.addActionRow(buttons.subList(i, Math.min(i + MAX_BUTTONS_PER_ROW, buttons.size())));
            }

            return builder.build();
        });
    }

    public CompletableFuture<Optional<TextChannel>> createTicketFromPanel(
        long userId,
        String categoryId) {
        return channelService.createTicket(userId, categoryId);
    }

    public CompletableFuture<Boolean> closeTicketFromPanel(long channelId, long staffId, String reason) {
        return channelService.closeTicket(channelId, staffId, reason);
    }

    public MessageEmbed createPanelEmbed() {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(this.config.panelEmbed.title)
            .setDescription(this.config.panelEmbed.description)
            .setColor(Color.decode(this.config.panelEmbed.color));

        if (isValid(this.config.panelEmbed.bannerUrl)) {
            builder.setImage(this.config.panelEmbed.bannerUrl);
        }

        if (isValid(this.config.panelEmbed.thumbnail)) {
            builder.setThumbnail(this.config.panelEmbed.thumbnail);
        }

        if (this.config.panelEmbed.footerText != null && !this.config.panelEmbed.footerText.trim().isEmpty()) {
            builder.setFooter(
                this.config.panelEmbed.footerText,
                isValid(this.config.panelEmbed.footerIcon) ? this.config.panelEmbed.footerIcon : null);
        }

        if (this.config.panelEmbed.showTimestamp) {
            builder.setTimestamp(Instant.now());
        }

        return builder.build();
    }
}
