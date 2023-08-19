package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.util.DiscordTagFormat;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class ServerCommand extends SlashCommand {

    private final AppConfig appConfig;

    public ServerCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "server";
        this.help = "Shows the server's information";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        String owner = DiscordTagFormat.memberTag(guild.getOwner().getUser());
        String id = guild.getId();
        String members = String.valueOf(guild.getMembers().size());
        String roles = String.valueOf(guild.getRoles().size());
        String channels = String.valueOf(guild.getChannels().size());
        String createdAt = DiscordTagFormat.offsetTime(guild.getTimeCreated());

        MessageEmbed embeds = new EmbedBuilder()
                .setTitle("🌐 | " + guild.getName() + "'s information")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(guild.getIconUrl())
                .addField("🔢 ID", id, false)
                .addField("👑 Owner", owner, false)
                .addField("👥 Members", members, false)
                .addField("📊 Roles", roles, false)
                .addField("📊 Channels", channels, false)
                .addField("📅 Created At", createdAt, false)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}