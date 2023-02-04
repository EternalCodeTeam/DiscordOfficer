package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

@UserPermissions(Permission.MANAGE_CHANNEL)
public class CooldownCommand extends ApplicationCommand {

    @JDASlashCommand(
            name = "cooldown",
            description = "Sets the cooldown of a command"
    )
    public void onSlashCommand(@NotNull GuildSlashEvent event, @AppOption(name = "cooldown") int cooldown) {
        if (cooldown > 21600) {
            MessageEmbed embeds = new Embeds().error
                    .setTitle("❌ | Error!")
                    .setDescription("The cooldown can't be higher than 21600 seconds!")
                    .build();

            event.replyEmbeds(embeds)
                    .setEphemeral(true)
                    .queue();
        }

        event.getChannel().asTextChannel().getManager().setSlowmode(cooldown).queue();

        MessageEmbed embeds = new Embeds().success
                .setTitle("✅ | Success!")
                .setDescription("This channel's cooldown is now " + cooldown + " seconds")
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}