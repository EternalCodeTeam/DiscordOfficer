package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class KickCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public KickCommand(DiscordAppConfig discordAppConfig) {
        this.name = "kick";
        this.help = "Kicks a user";
        this.userPermissions = new Permission[]{ Permission.KICK_MEMBERS };

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "select the user").setRequired(true),
                new OptionData(OptionType.STRING, "reason", "provide a reason").setRequired(false)
        );

        this.discordAppConfig = discordAppConfig;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            User user = event.getOption("user").getAsUser();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

            if (user.isBot()) {
                MessageEmbed embed = new EmbedBuilder()
                        .setColor(Color.decode(this.discordAppConfig.embedSettings.errorEmbed.color))
                        .setTitle("❌ | An error occurred while kicking the user")
                        .setDescription("You can't kick a bot")
                        .setTimestamp(Instant.now())
                        .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                        .build();

                event.replyEmbeds(embed)
                        .setEphemeral(true)
                        .queue();

                return;
            }

            user.openPrivateChannel().queue(channel -> {
                MessageEmbed embed = new EmbedBuilder()
                        .setColor(Color.decode(this.discordAppConfig.embedSettings.errorEmbed.color))
                        .setTitle("🔨 | You have been kicked from " + event.getGuild().getName())
                        .setDescription("Reason: " + reason)
                        .setTimestamp(Instant.now())
                        .build();

                channel.sendMessageEmbeds(embed).submit();
            });

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("✅ | Successfully kicked " + user.getAsTag())
                    .setDescription("Reason: " + reason)
                    .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                    .setTimestamp(Instant.now())
                    .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                    .build();

            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();

            event.getGuild().kick(user).reason(reason).queue();
        }
        catch (Exception ignored) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("❌ | An error occurred while kicking the user")
                    .setDescription("I can't kick this user, he probably has highest role than me!")
                    .setColor(Color.decode(this.discordAppConfig.embedSettings.errorEmbed.color))
                    .setTimestamp(Instant.now())
                    .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                    .build();

            event.replyEmbeds(embed)
                    .setEphemeral(true)
                    .queue();
        }
    }
}