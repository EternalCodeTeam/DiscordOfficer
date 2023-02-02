package com.eternalcode.discordapp.command;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class AvatarCommand extends ApplicationCommand {

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "avatar",
            description = "Shows the avatar of a user"
    )

    public void onSlashCommand(@NotNull GlobalSlashEvent event, @NotNull @TextOption User user) {
        event.deferReply().queue();

        event.getHook().sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle(user.getName() + "'s avatar")
                        .setImage(user.getEffectiveAvatarUrl() + "?size=2048")
                        .build()
        ).queue();
    }
}
