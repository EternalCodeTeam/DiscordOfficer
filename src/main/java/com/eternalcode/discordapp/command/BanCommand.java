package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand extends SlashCommand {

    private final AppConfig appConfig;

    public BanCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "ban";
        this.help = "Ban a user from the server";
        this.userPermissions = new Permission[]{ Permission.BAN_MEMBERS };
        this.options = List.of(
                new OptionData(OptionType.USER, "user", "user").setRequired(true),
                new OptionData(OptionType.INTEGER, "deletion-time", "deletion-time")
                        .addChoice("Don't delete", 0)
                        .addChoice("1 Hour", 1)
                        .addChoice("6 Hours", 6)
                        .addChoice("12 Hours", 12)
                        .addChoice("1 day", 24)
                        .addChoice("3 days", 72)
                        .addChoice("7 days", 168)
                        .setRequired(true),
                new OptionData(OptionType.STRING, "reason", "reason").setRequired(false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            User user = event.getOption("user").getAsUser();
            int deletionTime = event.getOption("deletion-time").getAsInt();
            String reason = event.getOption("reason").getAsString();
            String kickReason = "Reason: " + (reason.isEmpty() ? "No reason provided" : reason);

            if (user.isBot()) {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("❌ | An error occurred while banning the user")
                        .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                        .setThumbnail(this.appConfig.embedSettings.errorEmbed.thumbnail)
                        .setDescription("You can't ban a bot")
                        .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                        .setTimestamp(Instant.now())
                        .build();

                event.replyEmbeds(embed)
                        .setEphemeral(true)
                        .queue();

                return;
            }

            user.openPrivateChannel().queue(channel -> {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("🔨 | You have been banned from " + event.getGuild().getName())
                        .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                        .setDescription("Reason: " + kickReason)
                        .setTimestamp(Instant.now())
                        .build();

                channel.sendMessageEmbeds(embed).submit();
            });

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("✅ | Successfully banned " + user.getName())
                    .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                    .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                    .setDescription("Reason: " + kickReason)
                    .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();

            event.getGuild().ban(user, deletionTime, TimeUnit.HOURS).reason(reason).queue();
        }
        catch (Exception exception) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("❌ | An error occurred while banning the user")
                    .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                    .setThumbnail(this.appConfig.embedSettings.errorEmbed.thumbnail)
                    .setDescription("I can't ban this user, he probably has highest role than me!")
                    .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                    .setTimestamp(Instant.now())
                    .build();

            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();
        }
    }
}
