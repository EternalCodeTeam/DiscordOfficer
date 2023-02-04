package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class ServerCommand extends ApplicationCommand {

    @JDASlashCommand(name = "server", description = "Shows the server's information")
    public void onSlashCommand(@NotNull GuildSlashEvent event) {
        String owner = "<@" + event.getGuild().getOwnerId() + ">";
        String id = event.getGuild().getId();
        String members = String.valueOf(event.getGuild().getMembers().size());
        String roles = String.valueOf(event.getGuild().getRoles().size());
        String channels = String.valueOf(event.getGuild().getChannels().size());
        String createdAt = "<t:" + event.getGuild().getTimeCreated().toEpochSecond() + ":F>";

        MessageEmbed embeds = new Embeds().success
                .setTitle("🌐 | " + event.getGuild().getName() + "'s information")
                .addField("🔢 ID", id, false)
                .addField("👑 Owner", owner, false)
                .addField("👥 Members", members, false)
                .addField("📊 Roles", roles, false)
                .addField("📊 Channels", channels, false)
                .addField("📅 Created At", createdAt, false)
                .build();


        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}
