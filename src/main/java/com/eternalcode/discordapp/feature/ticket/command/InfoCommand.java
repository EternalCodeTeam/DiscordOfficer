package com.eternalcode.discordapp.feature.ticket.command;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.feature.ticket.TicketService;
import com.eternalcode.discordapp.feature.ticket.TicketWrapper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

class InfoCommand extends SlashCommand {
    private final TicketService ticketService;

    public InfoCommand(TicketService ticketService) {
        this.ticketService = ticketService;
        this.name = "info";
        this.help = "Displays information about the ticket.";
        this.userPermissions = new Permission[] {Permission.MANAGE_CHANNEL};
    }

    @Override
    public void execute(SlashCommandEvent event) {
        ticketService.getTicketByChannel(event.getChannel().getIdLong())
            .thenAccept(ticketOpt -> {
                if (ticketOpt.isEmpty()) {
                    event.reply("❌ This channel is not a ticket.")
                        .setEphemeral(true)
                        .queue();
                    return;
                }

                TicketWrapper ticket = ticketOpt.get();
                String info = String.format(
                    "🎫 **Ticket #%d**\n" +
                        "👤 **User:** <@%d>\n" +
                        "📋 **Category:** %s\n" +
                        "📅 **Created:** <t:%d:F>",
                    ticket.getId(),
                    ticket.getUserId(),
                    ticket.getCategory(),
                    ticket.getCreatedAt().getEpochSecond()
                );

                event.reply(info).setEphemeral(true).queue();
            })
            .exceptionally(throwable -> {
                FutureHandler.handleException(throwable);
                event.reply("❌ An error occurred while fetching the ticket information: " + throwable.getMessage())
                    .setEphemeral(true)
                    .queue();
                return null;
            });
    }
}
