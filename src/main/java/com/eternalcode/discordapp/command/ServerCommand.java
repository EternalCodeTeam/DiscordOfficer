package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class ServerCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public ServerCommand(DiscordAppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;

        this.name = "server";
        this.help = "Shows the server's information";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String owner = "<@" + event.getGuild().getOwnerId() + ">";
        String id = event.getGuild().getId();
        String members = String.valueOf(event.getGuild().getMembers().size());
        String roles = String.valueOf(event.getGuild().getRoles().size());
        String channels = String.valueOf(event.getGuild().getChannels().size());
        String createdAt = "<t:" + event.getGuild().getTimeCreated().toEpochSecond() + ":F>";

        MessageEmbed embeds = new EmbedBuilder()
                .setTitle("🌐 | " + event.getGuild().getName() + "'s information")
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .setThumbnail(event.getGuild().getIconUrl())
                .addField("🔢 ID", id, false)
                .addField("👑 Owner", owner, false)
                .addField("👥 Members", members, false)
                .addField("📊 Roles", roles, false)
                .addField("📊 Channels", channels, false)
                .addField("📅 Created At", createdAt, false)
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}