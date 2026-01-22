package com.eternalcode.discordapp.ticket.panel;

import com.eternalcode.discordapp.ticket.TicketConfig;
import com.eternalcode.discordapp.ticket.TicketService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TicketPanelController extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketPanelController.class);
    private static final String BUTTON_PREFIX = "ticket_";
    private static final int DELETE_DELAY_SECONDS = 5;

    private final TicketService ticketService;
    private final TicketPanelService panelService;
    private final TicketConfig config;

    public TicketPanelController(TicketService ticketService, TicketPanelService panelService, TicketConfig config) {
        this.ticketService = ticketService;
        this.panelService = panelService;
        this.config = config;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (!buttonId.startsWith(BUTTON_PREFIX)) {
            return;
        }

        switch (buttonId) {
            case "ticket_close" -> this.handleCloseButton(event);
            case "ticket_confirm_delete" -> this.handleConfirmDeleteButton(event);
            case "ticket_cancel" -> event.getMessage().delete().queue();
            default -> this.handleCategoryButton(event);
        }
    }

    private void handleCategoryButton(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        TicketConfig.TicketCategoryConfig category = this.config.getEnabledCategories().stream()
            .filter(cat -> cat.getButtonId().equals(event.getComponentId()))
            .findFirst()
            .orElse(null);

        if (category == null) {
            event.reply("❌ Unknown category.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        event.getHook().editOriginal("🔄 Creating ticket...").queue();

        this.ticketService.canUserCreateTicket(userId, category.id)
            .thenCompose(canCreate -> {
                if (!canCreate) {
                    event.getHook().editOriginal(this.config.messages.tooManyTickets).queue();
                    return CompletableFuture.completedFuture(null);
                }

                return this.panelService.createTicketFromPanel(userId, category.id)
                    .thenAccept(channelOpt -> {
                        String message = channelOpt
                            .map(textChannel -> "✅ Your ticket has been created: " + textChannel.getAsMention())
                            .orElse("❌ Failed to create ticket. Please try again later.");
                        event.getHook().editOriginal(message).queue();
                    });
            })
            .exceptionally(exception -> {
                LOGGER.error("Error creating ticket for user {}", userId, exception);
                this.safeEditHook(event, "❌ An error occurred. Please try again later.");
                return null;
            });
    }

    private void handleCloseButton(ButtonInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("⚠️ Confirm Ticket Closure")
            .setDescription(
                "Are you sure you want to **close and delete** this ticket?\n\n**Warning:** This action is irreversible!")
            .setColor(Color.ORANGE)
            .addField(
                "📋 What will happen:",
                "• Ticket will be closed\n• Channel will be deleted\n• All messages will be lost",
                false)
            .setFooter("Click the button below to confirm")
            .setTimestamp(Instant.now());

        event.reply(new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(
                    ActionRow.of(
                        Button.danger("ticket_confirm_delete", "🗑️ Yes, delete ticket"),
                        Button.secondary("ticket_cancel", "❌ Cancel")
                    )
                ).build())
            .setEphemeral(true)
            .queue();
    }

    private void handleConfirmDeleteButton(ButtonInteractionEvent event) {
        event.deferReply(true).queue();

        Channel channel = event.getChannel();
        long channelId = channel.getIdLong();
        long userId = event.getUser().getIdLong();

        for (int i = DELETE_DELAY_SECONDS; i >= 1; i--) {
            final int count = i;
            CompletableFuture.delayedExecutor(DELETE_DELAY_SECONDS - i, TimeUnit.SECONDS)
                .execute(() -> this.safeEditHook(
                    event,
                    "🗑️ Deleting ticket in " + count + " second" + (count > 1 ? "s" : "") + "..."));
        }

        CompletableFuture.delayedExecutor(DELETE_DELAY_SECONDS, TimeUnit.SECONDS)
            .execute(() -> {
                this.panelService.closeTicketFromPanel(channelId, userId, "Closed by user")
                    .thenAccept(success -> {
                        if (!success) {
                            this.safeEditHook(event, "❌ Failed to delete ticket.");
                            LOGGER.warn("Failed to close ticket {} for user {}", channelId, userId);
                        }
                    })
                    .exceptionally(exception -> {
                        LOGGER.error("Error deleting ticket {} for user {}", channelId, userId, exception);
                        this.safeEditHook(event, "❌ An error occurred. Please try again later.");
                        return null;
                    });
            });
    }

    private void safeEditHook(ButtonInteractionEvent event, String message) {
        try {
            event.getHook().editOriginal(message).queue();
        }
        catch (Exception exception) {
            LOGGER.debug("Failed to update hook", exception);
        }
    }
}
