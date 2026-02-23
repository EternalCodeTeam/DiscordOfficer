package com.eternalcode.discordapp.feature.ticket;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.commons.scheduler.loom.LoomScheduler;
import com.eternalcode.discordapp.feature.ticket.command.TicketCommand;
import com.eternalcode.discordapp.feature.ticket.panel.TicketPanelController;
import com.eternalcode.discordapp.feature.ticket.panel.TicketPanelService;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import java.time.Duration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketConfigurer {

    public static final Duration TICKET_CLEANUP_FREQUENCY = Duration.ofHours(1);
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketConfigurer.class);

    private final JDA jda;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final LoomScheduler scheduler;
    private final CommandClientBuilder commandClientBuilder;
    private final AppConfig appConfig;

    public TicketConfigurer(
        JDA jda,
        ConfigManager configManager,
        DatabaseManager databaseManager,
        LoomScheduler scheduler,
        CommandClientBuilder commandClientBuilder,
        AppConfig appConfig
    ) {
        this.jda = jda;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.scheduler = scheduler;
        this.commandClientBuilder = commandClientBuilder;
        this.appConfig = appConfig;
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

            // Manually register ticket command via JDA API as CommandClientBuilder may not register commands added after builder creation
            SlashCommandData ticketCommandData = Commands.slash("ticket", "Ticket management solution")
                .addSubcommands(
                    new SubcommandData("panel", "Displays the ticket panel."),
                    new SubcommandData("close", "Closes the ticket."),
                    new SubcommandData("info", "Displays information about the ticket."),
                    new SubcommandData("list", "Lists all active tickets.")
                );

            this.jda.getGuildById(this.appConfig.guildId).updateCommands()
                .addCommands(ticketCommandData)
                .queue(
                    success -> LOGGER.info("Ticket command manually registered via JDA API successfully"),
                    failure -> LOGGER.error("Failed to manually register ticket command via JDA API", failure)
                );

            // Schedules a task that periodically cleans up inactive tickets.
            // If an administrator deletes a ticket channel manually instead of clicking "Close ticket",
            // the record may remain in the database — this task removes such leftovers.
            this.scheduler.runAsyncTimer(
                () -> {
                    try {
                        ticketChannelService.cleanupInactiveTickets().join();
                    }
                    catch (Exception exception) {
                        LOGGER.error("Error during ticket cleanup", exception);
                    }
                }, Duration.ZERO, TICKET_CLEANUP_FREQUENCY);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to initialize ticket system", exception);
            throw new RuntimeException("Ticket system initialization failed", exception);
        }
    }
}

