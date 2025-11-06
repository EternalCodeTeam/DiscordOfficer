package com.eternalcode.discordapp.ticket.command;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.ticket.TicketChannelService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

class CloseCommand extends SlashCommand {
    private final TicketChannelService channelService;

    public CloseCommand(TicketChannelService channelService) {
        this.channelService = channelService;
        this.name = "close";
        this.help = "Closes the ticket.";
        this.userPermissions = new Permission[] {Permission.MANAGE_CHANNEL};
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String reason = "Closed via command";

        channelService.closeTicket(
            event.getChannel().getIdLong(),
            event.getUser().getIdLong(),
            reason
        ).thenAccept(success -> {
            if (success) {
                event.reply("✅ The ticket has been successfully closed.")
                    .setEphemeral(true)
                    .queue();
            }
            else {
                event.reply("❌ Failed to close the ticket.")
                    .setEphemeral(true)
                    .queue();
            }
        }).exceptionally(throwable -> {
            FutureHandler.handleException(throwable);
            event.reply("❌ An error occurred while closing the ticket: " + throwable.getMessage())
                .setEphemeral(true)
                .queue();
            return null;
        });
    }
}
