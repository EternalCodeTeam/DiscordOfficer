package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class AvatarCommand extends ApplicationCommand {

    @JDASlashCommand(name = "avatar", description = "Shows the avatar of a user")
    public void onSlashCommand(@NotNull GuildSlashEvent event, @NotNull @AppOption(name = "user") User user) {
        MessageEmbed build = new EmbedBuilder()
                .setTitle(user.getName() + "'s avatar")
                .setImage(user.getEffectiveAvatarUrl() + "?size=2048")
                .build();

        event.replyEmbeds(build)
                .setEphemeral(true)
                .queue();
    }
}
