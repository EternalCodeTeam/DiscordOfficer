package com.eternalcode.discordapp.ticket;

import dev.skywolfxp.transcript.Transcript;
import java.awt.Color;
import java.io.File;
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
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketChannelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketChannelService.class);

    private final JDA jda;
    private final TicketConfig config;
    private final TicketService ticketService;

    public TicketChannelService(JDA jda, TicketConfig config, TicketService ticketService) {
        this.jda = jda;
        this.config = config;
        this.ticketService = ticketService;
    }

    public CompletableFuture<Optional<TextChannel>> createTicket(long userId, String categoryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TicketConfig.TicketCategoryConfig category = config.getCategoryById(categoryId);
                if (category == null || !category.enabled || ticketService.canUserCreateTicket(userId, categoryId)) {
                    return Optional.empty();
                }

                long ticketId = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
                TextChannel channel = createChannel(userId, category, ticketId);

                ticketService.saveTicket(new TicketWrapper(ticketId, userId, channel.getIdLong(), categoryId));
                channel.sendMessage(createWelcomeMessage(ticketId, userId, category)).queue();

                LOGGER.info("Created ticket #{} for user {}", ticketId, userId);
                return Optional.of(channel);
            }
            catch (Exception e) {
                LOGGER.error("Failed to create ticket for user {}: {}", userId, e.getMessage(), e);
                return Optional.empty();
            }
        });
    }

    public CompletableFuture<Boolean> closeTicket(long channelId, long staffId, String reason) {
        return ticketService.getTicketByChannel(channelId).thenCompose(ticketOpt -> {
            if (ticketOpt.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            TicketWrapper ticket = ticketOpt.get();
            generateTranscript(ticket, staffId, reason);

            return ticketService.deleteTicket(ticket.getId()).thenApply(result -> {
                if (result > 0) {
                    TextChannel channel = jda.getTextChannelById(channelId);
                    if (channel != null) {
                        channel.delete().queue();
                    }
                    return true;
                }
                return false;
            });
        });
    }

    public CompletableFuture<Void> cleanupInactiveTickets() {
        return ticketService.findInactiveTickets(Instant.now().minus(config.getAutoCloseDuration()))
            .thenCompose(tickets -> CompletableFuture.allOf(
                tickets.stream()
                    .map(t -> closeTicket(t.getChannelId(), 0L, "Automatically closed due to inactivity")
                        .thenApply(r -> (Void) null))
                    .toArray(CompletableFuture[]::new)
            ));
    }

    public MessageCreateData createWelcomeMessage(
        long ticketId,
        long userId,
        TicketConfig.TicketCategoryConfig category) {
        return new MessageCreateBuilder()
            .setContent("<@" + userId + ">")
            .setEmbeds(new EmbedBuilder()
                .setTitle("🎫 Ticket #" + ticketId)
                .setDescription(config.messages.ticketCreated)
                .addField("📋 Category", category.displayName, true)
                .addField("👤 User", "<@" + userId + ">", true)
                .addField("📅 Created", "<t:" + Instant.now().getEpochSecond() + ":F>", true)
                .setColor(Color.decode(config.embeds.color))
                .setTimestamp(config.embeds.showTimestamp ? Instant.now() : null)
                .build())
            .addActionRow(Button.danger("ticket_close", "🔒 Close"))
            .build();
    }

    public MessageEmbed createEmbed(String title, String description) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.decode(config.embeds.color));

        if (isValidUrl(config.embeds.thumbnail)) {
            builder.setThumbnail(config.embeds.thumbnail);
        }

        if (config.embeds.footerText != null && !config.embeds.footerText.trim().isEmpty()) {
            builder.setFooter(
                config.embeds.footerText,
                isValidUrl(config.embeds.footerIcon) ? config.embeds.footerIcon : null);
        }

        if (config.embeds.showTimestamp) {
            builder.setTimestamp(Instant.now());
        }

        return builder.build();
    }

    private TextChannel createChannel(long userId, TicketConfig.TicketCategoryConfig category, long ticketId) {
        Category cat = jda.getCategoryById(config.categoryId);
        if (cat == null) {
            throw new IllegalStateException("Category not found: " + config.categoryId);
        }

        User user = jda.getUserById(userId);
        String userName = user != null ? user.getName() : "user" + userId;
        int count = ticketService.countTicketsByUser(userId).join();
        String channelName = count == 0 ? "ticket-" + userName : "ticket-" + userName + "-" + (count + 1);

        TextChannel channel = cat.createTextChannel(channelName)
            .setTopic("Ticket " + category.displayName)
            .complete();

        setupPermissions(channel, userId);
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
                    List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY), null)
                .queue();
        }

        if (config.staffRoleId != 0L) {
            Role staffRole = guild.getRoleById(config.staffRoleId);
            if (staffRole != null) {
                channel.getManager()
                    .putPermissionOverride(
                        staffRole,
                        List.of(
                            Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                            Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE), null)
                    .queue();
            }
        }
    }

    private void generateTranscript(TicketWrapper ticket, long staffId, String reason) {
        if (config.transcriptChannelId == 0L) {
            return;
        }

        MessageChannel transcriptChannel = jda.getChannelById(MessageChannel.class, config.transcriptChannelId);
        TextChannel ticketChannel = jda.getTextChannelById(ticket.getChannelId());

        if (transcriptChannel == null || ticketChannel == null) {
            return;
        }

        try {
            Transcript transcript = new Transcript();
            transcript.render(ticketChannel);

            File file = File.createTempFile("transcript-" + ticket.getId(), ".html");
            transcript.writeToFile(file);

            String message = String.format("📄 **Transcript Ticket #%d**%n> Closed by: %s%n> Reason: %s",
                ticket.getId(),
                staffId == 0L ? "Automatic" : "<@" + staffId + ">",
                reason);

            transcriptChannel.sendMessage(message)
                .addFiles(FileUpload.fromData(file))
                .queue(s -> file.delete(), e -> file.delete());
        }
        catch (Exception exception) {
            LOGGER.error("Failed to generate transcript for ticket #{}", ticket.getId(), exception);
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.trim().isEmpty() &&
            (url.startsWith("http://") || url.startsWith("https://"));
    }
}
