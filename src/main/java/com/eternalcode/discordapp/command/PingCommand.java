package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends ApplicationCommand {

    @JDASlashCommand(name = "ping", description = "Pong!")
    public void onSlashCommand(@NotNull GuildSlashEvent event) {
        event.deferReply().queue();
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();
        
        event.replyEmbeds(new Embeds().success
                .setTitle("🏓 | Pong!")
                .addField("Gateway Ping", String.valueOf(gatewayPing), true)
                .addField("Rest Ping", String.valueOf(restPing), true)
                .build()).queue();
    }
}
