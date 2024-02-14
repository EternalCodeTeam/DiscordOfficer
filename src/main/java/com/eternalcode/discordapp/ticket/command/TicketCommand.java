package com.eternalcode.discordapp.ticket.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.ticket.command.alias.StartTicketCommand;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public class TicketCommand extends SlashCommand {
    public TicketCommand(AppConfig appConfig) {
        this.name = "ticket";
        this.help = "huj";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.children = new SlashCommand[]{
            new StartTicketCommand(appConfig)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        /* This method is empty because uses children for sub-commands. */
    }
}
