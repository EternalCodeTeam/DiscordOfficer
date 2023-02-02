package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@UserPermissions(Permission.KICK_MEMBERS)
public class KickCommand extends ApplicationCommand {

    @JDASlashCommand(name = "kick", description = "Kicks a user")
    public void onSlashCommand(@NotNull GuildSlashEvent event, @NotNull @AppOption(name = "user") User user, @Optional @AppOption(name = "reason") String reason) {
        event.getGuild().kick(user).reason(reason).queue();

        MessageEmbed build = new EmbedBuilder()
                .setTimestamp(event.getTimeCreated())
                .setAuthor(event.getMember().getEffectiveName())
                .setTitle("Successfully kicked " + user.getAsTag())
                .setColor(Color.CYAN)
                .setDescription("Reason: " + (reason != null ? reason : "No reason provided"))
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
