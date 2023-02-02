package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CooldownCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "cooldown",
            description = "Sets the cooldown of a command"
    )

    public void onSlashCommand(@NotNull GlobalSlashEvent event, @TextOption int cooldown) {
        event.deferReply().queue();
        event.getChannel().asTextChannel().getManager().setSlowmode(cooldown).queue();

        event.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Succesfully changed cooldown!")
                        .setAuthor(event.getMember().getEffectiveName())
                        .setDescription("Cooldown set to " + cooldown + " seconds.")
                        .setTimestamp(event.getTimeCreated())
                        .build()
        ).queue();
    }
}
