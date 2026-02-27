package com.eternalcode.discordapp.feature.ticket;

import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.scheduler.Scheduler;
import com.eternalcode.discordapp.feature.ticket.command.TicketCommand;
import com.eternalcode.discordapp.feature.ticket.panel.TicketPanelController;
import com.eternalcode.discordapp.feature.ticket.panel.TicketPanelService;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.time.Duration;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketConfigurer {

    public static final Duration TICKET_CLEANUP_FREQUENCY = Duration.ofHours(1);
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketConfigurer.class);

    private final JDA jda;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final Scheduler scheduler;
    private final CommandClientBuilder commandClientBuilder;

    public TicketConfigurer(
        JDA jda,
        ConfigManager configManager,
        DatabaseManager databaseManager,
        Scheduler scheduler,
        CommandClientBuilder commandClientBuilder
    ) {
        this.jda = jda;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.scheduler = scheduler;
        this.commandClientBuilder = commandClientBuilder;
    }

    public void initialize() {
        try {
            TicketConfig ticketConfig = this.configManager.load(new TicketConfig());
            TicketRepository ticketRepository = TicketRepository.create(this.databaseManager);
            TicketService ticketService = new TicketService(ticketConfig, ticketRepository);

            TranscriptGeneratorService transcriptGeneratorService = new TranscriptGeneratorService(
                this.jda,
                ticketConfig
            );
            TicketChannelService ticketChannelService = new TicketChannelService(
                this.jda,
                ticketConfig,
                ticketService,
                transcriptGeneratorService
            );
            TicketPanelService ticketPanelService = new TicketPanelService(
                ticketChannelService,
                ticketConfig
            );
            TicketPanelController ticketPanelController = new TicketPanelController(
                ticketService,
                ticketPanelService,
                ticketConfig
            );

            this.jda.addEventListener(ticketPanelController);

            TicketCommand ticketCommand = new TicketCommand(
                ticketService,
                ticketChannelService,
                ticketPanelService
            );

            this.commandClientBuilder.addSlashCommands(ticketCommand);

            // Schedules a task that periodically cleans up inactive tickets.
            // If an administrator deletes a ticket channel manually instead of clicking "Close ticket",
            // the record may remain in the database — this task removes such leftovers.
            this.scheduler.scheduleRepeating(
                () -> {
                    try {
                        ticketChannelService.cleanupInactiveTickets().join();
                    }
                    catch (Exception exception) {
                        LOGGER.error("Error during ticket cleanup", exception);
                    }
                }, TICKET_CLEANUP_FREQUENCY);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to initialize ticket system", exception);
            throw new RuntimeException("Ticket system initialization failed", exception);
        }
    }
}

