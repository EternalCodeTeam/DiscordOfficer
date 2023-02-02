package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class KickCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "kick",
            description = "Kicks a user"
    )

    public void onSlashCommand(@NotNull GlobalSlashEvent event, @NotNull @TextOption User user, @Optional @TextOption String reason) {
        event.deferReply().queue();

        event.getGuild().kick(user).reason(reason).queue();

        event.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setTimestamp(event.getTimeCreated())
                        .setAuthor(event.getMember().getEffectiveName())
                        .setTitle("Succesfully kicked " + user.getAsTag())
                        .setColor(Color.CYAN)
                        .setDescription("Reason: " + reason)
                        .build()
        ).queue();
    }
}
