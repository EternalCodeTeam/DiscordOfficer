package com.eternalcode.discordapp.ticket;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketChannelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketChannelService.class);

    private final JDA jda;
    private final TicketConfig config;
    private final TicketService ticketService;
    private final TranscriptGeneratorService transcriptGeneratorService;

    public TicketChannelService(
        JDA jda,
        TicketConfig config,
        TicketService ticketService,
        TranscriptGeneratorService transcriptGeneratorService) {
        this.jda = jda;
        this.config = config;
        this.ticketService = ticketService;
        this.transcriptGeneratorService = transcriptGeneratorService;
    }

    public CompletableFuture<Optional<TextChannel>> createTicket(long userId, String categoryId) {
        TicketConfig.TicketCategoryConfig category = this.config.getCategoryById(categoryId);
        if (category == null || !category.enabled) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return this.ticketService.canUserCreateTicket(userId, categoryId)
            .thenCompose(canCreate -> {
                if (!canCreate) {
                    return CompletableFuture.completedFuture(Optional.empty());
                }

                return CompletableFuture.supplyAsync(() -> {
                    try {
                        long ticketId = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
                        TextChannel channel = this.createChannel(userId, category, ticketId);

                        TicketWrapper ticket = new TicketWrapper(userId, channel.getIdLong(), categoryId);
                        this.ticketService.saveTicket(ticket).join();

                        channel.sendMessage(this.createWelcomeMessage(ticketId, userId, category)).queue();

                        LOGGER.info(
                            "Created ticket #{} for user {} in channel {}",
                            ticketId,
                            userId,
                            channel.getIdLong());
                        return Optional.of(channel);
                    }
                    catch (Exception exception) {
                        LOGGER.error("Failed to create ticket for user {}", userId, exception);
                        return Optional.empty();
                    }
                });
            });
    }

    public CompletableFuture<Boolean> closeTicket(long channelId, long staffId, String reason) {
        return this.ticketService.getTicketByChannel(channelId).thenCompose(ticketOpt -> {
            if (ticketOpt.isEmpty()) {
                LOGGER.warn("Attempted to close non-existent ticket for channel {}", channelId);
                return CompletableFuture.completedFuture(false);
            }

            TicketWrapper ticket = ticketOpt.get();

            return this.ticketService.deleteTicket(ticket.getId()).thenApply(result -> {
                if (result > 0) {
                    this.transcriptGeneratorService.generateAndSendTranscript(ticket, staffId, reason);

                    TextChannel channel = this.jda.getTextChannelById(channelId);
                    if (channel != null) {
                        channel.delete().queue(
                            success -> LOGGER.info("Successfully closed and deleted ticket #{}", ticket.getId()),
                            failure -> LOGGER.error(
                                "Failed to delete channel {} for ticket #{}",
                                channelId,
                                ticket.getId(),
                                failure)
                        );
                    }
                    return true;
                }
                LOGGER.warn("Failed to delete ticket #{} from database", ticket.getId());
                return false;
            });
        }).exceptionally(exception -> {
            LOGGER.error("Error closing ticket for channel {}", channelId, exception);
            return false;
        });
    }

    public CompletableFuture<Void> cleanupInactiveTickets() {
        Instant cutoffTime = Instant.now().minus(this.config.getAutoCloseDuration());

        return this.ticketService.findTicketsOlderThan(cutoffTime)
            .thenCompose(tickets -> CompletableFuture.allOf(
                tickets.stream()
                    .map(ticket -> this.closeTicket(
                            ticket.getChannelId(),
                            0L,
                            "Automatically closed due to inactivity")
                        .thenApply(result -> (Void) null))
                    .toArray(CompletableFuture[]::new)
            ));
    }

    public MessageCreateData createWelcomeMessage(
        long ticketId,
        long userId,
        TicketConfig.TicketCategoryConfig category) {

        Instant now = Instant.now();

        return new MessageCreateBuilder()
            .setContent("<@" + userId + ">")
            .setEmbeds(new EmbedBuilder()
                .setTitle("🎫 Ticket #" + ticketId)
                .setDescription(this.config.messages.ticketCreated)
                .addField("📋 Category", category.displayName, true)
                .addField("👤 User", "<@" + userId + ">", true)
                .addField("📅 Created", "<t:" + now.getEpochSecond() + ":F>", true)
                .setColor(Color.decode(this.config.embeds.color))
                .setTimestamp(this.config.embeds.showTimestamp ? now : null)
                .build())
            .addActionRow(Button.danger("ticket_close", "🔒 Close"))
            .build();
    }

    public MessageEmbed createEmbed(String title, String description) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.decode(this.config.embeds.color));

        if (this.isValidUrl(this.config.embeds.thumbnail)) {
            builder.setThumbnail(this.config.embeds.thumbnail);
        }

        if (this.config.embeds.footerText != null && !this.config.embeds.footerText.trim().isEmpty()) {
            builder.setFooter(
                this.config.embeds.footerText,
                this.isValidUrl(this.config.embeds.footerIcon) ? this.config.embeds.footerIcon : null);
        }

        if (this.config.embeds.showTimestamp) {
            builder.setTimestamp(Instant.now());
        }

        return builder.build();
    }

    private TextChannel createChannel(long userId, TicketConfig.TicketCategoryConfig category, long ticketId) {
        Category cat = this.jda.getCategoryById(this.config.categoryId);
        if (cat == null) {
            throw new IllegalStateException("Category not found: " + this.config.categoryId);
        }

        User user = this.jda.getUserById(userId);
        String userName = user != null ? user.getName() : "user" + userId;
        long count = this.ticketService.countTicketsByUser(userId).join();
        String channelName = count == 0 ? "ticket-" + userName : "ticket-" + userName + "-" + (count + 1);

        TextChannel channel = cat.createTextChannel(channelName)
            .setTopic("Ticket " + category.displayName)
            .complete();

        this.setupPermissions(channel, userId);
        return channel;
    }

    private void setupPermissions(TextChannel channel, long userId) {
        Guild guild = channel.getGuild();

        channel.getManager()
            .putPermissionOverride(guild.getPublicRole(), null, List.of(Permission.VIEW_CHANNEL))
            .queue();

        Member member = guild.getMemberById(userId);
        if (member != null) {
            channel.getManager()
                .putPermissionOverride(
                    member,
                    List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                    null)
                .queue();
        }

        if (this.config.staffRoleId != 0L) {
            Role staffRole = guild.getRoleById(this.config.staffRoleId);
            if (staffRole != null) {
                channel.getManager()
                    .putPermissionOverride(
                        staffRole,
                        List.of(
                            Permission.VIEW_CHANNEL,
                            Permission.MESSAGE_SEND,
                            Permission.MESSAGE_HISTORY,
                            Permission.MESSAGE_MANAGE),
                        null)
                    .queue();
            }
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.trim().isEmpty() &&
            (url.startsWith("http://") || url.startsWith("https://"));
    }
}
