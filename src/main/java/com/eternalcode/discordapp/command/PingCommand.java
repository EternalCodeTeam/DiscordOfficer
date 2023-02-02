package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends ApplicationCommand {

    @JDASlashCommand(name = "ping", description = "Pong!")
    public void onSlashCommand(@NotNull GuildSlashEvent event) {
        event.deferReply().queue();

        long gatewayPing = event.getJDA().getGatewayPing();
        event.getJDA().getRestPing()
                .queue(l -> event.getHook()
                        .sendMessageFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l)
                        .queue());
    }
}
