package com.eternalcode.discordapp.feature.ticket.command;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.feature.ticket.TicketService;
import com.eternalcode.discordapp.feature.ticket.TicketWrapper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

class ListCommand extends SlashCommand {
    private final TicketService ticketService;

    public ListCommand(TicketService ticketService) {
        this.ticketService = ticketService;
        this.name = "list";
        this.help = "Lists all active tickets.";
        this.userPermissions = new Permission[] {Permission.MANAGE_CHANNEL};
    }

    private static @NotNull StringBuilder createTicketOverview(List<TicketWrapper> tickets) {
        StringBuilder response = new StringBuilder("📋 **Your tickets:**\n\n");
        for (TicketWrapper ticket : tickets) {
            response.append(String.format(
                "🎫 **#%d** - %s - <#%d>\n",
                ticket.getId(),
                ticket.getCategory(),
                ticket.getChannelId()
            ));
        }

        if (response.length() > 2000) {
            response.setLength(1997);
            response.append("...");
        }
        return response;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        long userId = event.getUser().getIdLong();

        ticketService.getUserTickets(userId)
            .thenAccept(tickets -> {
                if (tickets.isEmpty()) {
                    event.reply("📋 You don’t have any active tickets.")
                        .setEphemeral(true)
                        .queue();
                    return;
                }

                StringBuilder response = createTicketOverview(tickets);

                event.reply(response.toString())
                    .setEphemeral(true)
                    .queue();
            })
            .exceptionally(throwable -> {
                FutureHandler.handleException(throwable);
                event.reply("❌ An error occurred while fetching the ticket list: " + throwable.getMessage())
                    .setEphemeral(true)
                    .queue();
                return null;
            });
    }
}

