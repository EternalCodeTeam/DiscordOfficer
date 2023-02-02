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

@UserPermissions(Permission.MANAGE_CHANNEL)
public class CooldownCommand extends ApplicationCommand {

    @JDASlashCommand(
            name = "cooldown",
            description = "Sets the cooldown of a command"
    )
    public void onSlashCommand(@NotNull GuildSlashEvent event, @AppOption(name = "cooldown") int cooldown) {
        event.getChannel().asTextChannel().getManager().setSlowmode(cooldown).queue();

        // best practice
        MessageEmbed build = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle("Successfully changed cooldown!")
                .setAuthor(event.getMember().getEffectiveName())
                .setDescription("Cooldown set to " + cooldown + " seconds.")
                .setTimestamp(event.getTimeCreated())
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
