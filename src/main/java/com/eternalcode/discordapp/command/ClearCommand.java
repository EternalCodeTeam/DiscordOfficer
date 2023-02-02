package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ClearCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "clear",
            description = "Clears a certain amount of messages in the chat."
    )

    public void onSlashCommand(@NotNull GlobalSlashEvent event, @TextOption int amount) {
        event.deferReply().queue();

        event.getChannel().getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
            event.getChannel().purgeMessages(messages);
        });

        event.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Success!")
                        .setDescription("Succesfully cleared " + amount + " messages!")
                        .setAuthor(event.getMember().getEffectiveName())
                        .setTimestamp(event.getTimeCreated())
                        .build()
        ).queue();
    }
}
