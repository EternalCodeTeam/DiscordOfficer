package com.eternalcode.discordapp.feature.ticket.command;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.feature.ticket.panel.TicketPanelService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

class PanelCommand extends SlashCommand {
    private final TicketPanelService panelService;

    public PanelCommand(TicketPanelService panelService) {
        this.panelService = panelService;
        this.name = "panel";
        this.help = "Displays the ticket panel.";
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR};
    }

    @Override
    public void execute(SlashCommandEvent event) {
        TextChannel targetChannel = event.getChannel().asTextChannel();

        panelService.createTicketPanel()
            .thenAccept(panelMessage -> {
                targetChannel.sendMessage(panelMessage).queue(
                    success -> event.reply("✅ The ticket panel has been sent to " + targetChannel.getAsMention())
                        .setEphemeral(true)
                        .queue(),
                    failure -> event.reply("❌ Failed to send the ticket panel: " + failure.getMessage())
                        .setEphemeral(true)
                        .queue()
                );
            })
            .exceptionally(throwable -> {
                FutureHandler.handleException(throwable);
                event.reply("❌ An error occurred while creating the ticket panel: " + throwable.getMessage())
                    .setEphemeral(true)
                    .queue();
                return null;
            });
    }
}
