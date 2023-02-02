package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "ping",
            description = "Pong!"
    )
    public void onSlashCommand(@NotNull GlobalSlashEvent event) {
        event.deferReply().queue();

        long gatewayPing = event.getJDA().getGatewayPing();
        event.getJDA().getRestPing()
                .queue(l -> event.getHook()
                        .sendMessageFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l)
                        .queue());
    }
}
