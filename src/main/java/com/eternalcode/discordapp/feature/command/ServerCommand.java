package com.eternalcode.discordapp.feature.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.util.DiscordTagFormat;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class ServerCommand extends SlashCommand {
    private final AppConfig appConfig;

    public ServerCommand(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.name = "server";
        this.help = "Shows detailed information about the server";
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command must be used in a server!")
                .setEphemeral(true)
                .queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("🌐 | " + guild.getName() + "'s Information")
            .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
            .setThumbnail(guild.getIconUrl())
            .setTimestamp(Instant.now())
            .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl());

        builder.addField("🔢 ID", guild.getId(), true)
            .addField("👑 Owner", DiscordTagFormat.memberTag(guild.getOwner() != null ? guild.getOwner().getUser() : null), true)
            .addField("📅 Created At", DiscordTagFormat.offsetTime(guild.getTimeCreated()), true);

        long memberCount = guild.getMemberCount();
        long onlineCount = guild.getMembers().stream()
            .filter(m -> m.getOnlineStatus() == OnlineStatus.ONLINE)
            .count();
        long botCount = guild.getMembers().stream()
            .filter(m -> m.getUser().isBot())
            .count();
        builder.addField("👥 Members", String.valueOf(memberCount), true)
            .addField("📊 Roles", String.valueOf(guild.getRoles().size()), true)
            .addField("📡 Channels", String.valueOf(guild.getChannels().size()), true);

        builder.addField("🚀 Boost Level", guild.getBoostTier().name().split("_")[1], true)
            .addField("✅ Verification", guild.getVerificationLevel().name(), true)
            .addField("👤 Online", String.valueOf(onlineCount), true)
            .addField("🤖 Bots", String.valueOf(botCount), true);

        if (guild.getBannerUrl() != null) {
            builder.setImage(guild.getBannerUrl() + "?size=1024");
        }

        MessageEmbed embed = builder.build();

        event.replyEmbeds(embed)
            .setEphemeral(false)
            .queue();
    }
}

