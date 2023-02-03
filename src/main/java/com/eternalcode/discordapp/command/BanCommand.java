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

import java.util.concurrent.TimeUnit;

@UserPermissions(Permission.BAN_MEMBERS)
public class BanCommand extends ApplicationCommand {

    @JDASlashCommand(name = "kick", description = "Bans a user")
    public void onSlashCommand(@NotNull GuildSlashEvent event,
                               @NotNull @AppOption(name = "user") User user,
                               @AppOption(name = "deletionTimeFrame") int deletionTimeFrame,
                               @Optional @AppOption(name = "reason") String reason) {
        try {
            String kickReason = "Reason: " + (reason != null ? reason : "No reason provided");

            user.openPrivateChannel().queue(privateChannel -> {
                MessageEmbed embed = new Embeds().error
                        .setTitle("You have been banned from " + event.getGuild().getName())
                        .setDescription("Reason: " + kickReason)
                        .build();

                privateChannel.sendMessageEmbeds(embed).queue();
            });

            MessageEmbed build = new Embeds().success
                    .setTitle("✅ | Successfully banned " + user.getAsTag())
                    .setDescription("Reason: " + kickReason)
                    .build();

            event.replyEmbeds(build)
                    .setEphemeral(true)
                    .queue();

            event.getGuild().ban(user, deletionTimeFrame, TimeUnit.DAYS).reason(reason).queue();
        } catch (Exception ignored) {
            MessageEmbed embed = new Embeds().error
                    .setDescription("I do not have the permission to ban this user!")
                    .build();

            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();
        }
    }
}
