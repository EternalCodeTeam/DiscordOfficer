package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@UserPermissions(Permission.MESSAGE_MANAGE)
public class ClearCommand extends ApplicationCommand {

    @JDASlashCommand(name = "clear", description = "Clears a certain amount of messages in the chat.")
    public void onSlashCommand(@NotNull GuildSlashEvent event, @AppOption(name = "amount") int amount) {
        event.getChannel().getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
            event.getChannel().purgeMessages(messages);
        });

        MessageEmbed build = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("Success!")
                .setDescription("Successfully cleared " + amount + " messages!")
                .setAuthor(event.getMember().getEffectiveName())
                .setTimestamp(event.getTimeCreated())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
