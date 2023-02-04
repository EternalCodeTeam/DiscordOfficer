package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.annotations.Optional;
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
public class EmbedCommand extends ApplicationCommand {

    @JDASlashCommand(
            name = "embed",
            description = "Send embed message"
    )
    public void onSlashCommand(
            @NotNull GuildSlashEvent event,
            @NotNull @AppOption(name = "title") String title,
            @NotNull @AppOption(name = "description") String description,
            @Optional @AppOption(name = "color") String color,
            @Optional @AppOption(name = "image-url") String image
    ) {
        try {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(title);
            embed.setDescription(description);
            embed.setColor(Color.decode(color != null ? color : "#FFFFFF"));
            embed.setThumbnail(image);

            MessageEmbed messageEmbed = embed.build();

            MessageEmbed successSend = new Embeds().success
                    .setDescription("Embed sended sucessfully")
                    .build();

            event.getChannel()
                    .sendMessageEmbeds(messageEmbed)
                    .queue();

            event.replyEmbeds(successSend)
                    .setEphemeral(true)
                    .queue();
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
