package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends ApplicationCommand {

    @JDASlashCommand(
            name = "ping",
            description = "Pong!"
    )
    public void onSlashCommand(@NotNull GuildSlashEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        MessageEmbed build = new Embeds().success
                .setTitle("🏓 | Pong!")
                .addField("Gateway Ping", String.valueOf(gatewayPing), true)
                .addField("Rest Ping", String.valueOf(restPing), true)
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
