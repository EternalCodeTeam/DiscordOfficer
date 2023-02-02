package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BotInfoCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "botinfo",
            description = "Shows information about the bot"
    )

    public void onSlashCommand(@NotNull GlobalSlashEvent event) {
        event.deferReply().queue();

        event.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Bot Information")
                        .setTimestamp(event.getTimeCreated())
                        .setAuthor(event.getMember().getEffectiveName())
                        .setImage(event.getGuild().getIconUrl())
                        .addField("Bot Name", event.getJDA().getSelfUser().getName(), true)
                        .addField("Bot ID", event.getJDA().getSelfUser().getId(), true)
                        .addField("Bot Owner", event.getJDA().retrieveApplicationInfo().complete().getOwner().getAsTag(), true)
                        .addField("Guilds", String.valueOf(event.getJDA().getGuilds().size()), true)
                        .addField("Users", String.valueOf(event.getJDA().getUsers().size()), true)
                        .addField("Gateway Ping", String.valueOf(event.getJDA().getGatewayPing()), true)
                        .addField("Rest Ping", String.valueOf(event.getJDA().getRestPing().complete()), true)
                        .build()
        ).queue();

    }

}
