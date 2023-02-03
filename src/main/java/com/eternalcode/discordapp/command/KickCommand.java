package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.annotations.Optional;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@UserPermissions(Permission.KICK_MEMBERS)
public class KickCommand extends ApplicationCommand {

    @JDASlashCommand(name = "kick", description = "Kicks a user")
    public void onSlashCommand(@NotNull GuildSlashEvent event, @NotNull @AppOption(name = "user") User user, @Optional @AppOption(name = "reason") String reason) {
        event.getGuild().kick(user).reason(reason).queue();

        MessageEmbed build = new Embeds().success
                .setTitle("✅ | Successfully kicked " + user.getAsTag())
                .setDescription("Reason: " + (reason != null ? reason : "No reason provided"))
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
