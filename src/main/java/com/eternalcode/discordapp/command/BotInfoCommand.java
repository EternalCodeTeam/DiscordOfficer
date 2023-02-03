package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class BotInfoCommand extends ApplicationCommand {

    @JDASlashCommand(name = "botinfo", description = "Shows information about the bot")
    public void onSlashCommand(@NotNull GuildSlashEvent event) {
        MessageEmbed build = new Embeds().success
                .setTitle("ℹ️ | Bot Information")
                .setImage(event.getGuild().getIconUrl())
                .addField("Guilds", String.valueOf(event.getJDA().getGuilds().size()), true)
                .addField("Users", String.valueOf(event.getJDA().getUsers().size()), true)
                .addField("Gateway Ping", String.valueOf(event.getJDA().getGatewayPing()), true)
                .addField("Rest Ping", String.valueOf(event.getJDA().getRestPing().complete()), true)
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }

}
