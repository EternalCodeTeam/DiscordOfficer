package com.eternalcode.discordapp.ticket.command;

import com.eternalcode.discordapp.ticket.TicketChannelService;
import com.eternalcode.discordapp.ticket.TicketService;
import com.eternalcode.discordapp.ticket.panel.TicketPanelService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public class TicketCommand extends SlashCommand {

    public TicketCommand(
        TicketService ticketService,
        TicketChannelService channelService,
        TicketPanelService panelService
    ) {
        this.name = "ticket";
        this.help = "Ticket management solution";
        this.userPermissions = new Permission[] {Permission.MANAGE_CHANNEL};

        this.children = new SlashCommand[] {
            new PanelCommand(panelService),
            new CloseCommand(channelService),
            new InfoCommand(ticketService),
            new ListCommand(ticketService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // This method won't be executed when using children
    }
}
