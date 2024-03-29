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

public class KickCommand extends SlashCommand {

    private final AppConfig appConfig;

    public KickCommand(AppConfig appConfig) {
        this.name = "kick";
        this.help = "Kicks a user";
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };

        this.options = List.of(
            new OptionData(OptionType.USER, "user", "select the user")
                .setRequired(true),
            new OptionData(OptionType.STRING, "reason", "provide a reason")
                .setRequired(false)
        );

        this.appConfig = appConfig;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            User user = event.getOption("user").getAsUser();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

            if (user.isBot()) {
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle("❌ | An error occurred while kicking the user")
                    .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                    .setThumbnail(this.appConfig.embedSettings.errorEmbed.thumbnail)
                    .setDescription("You can't kick a bot")
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
                    .setTitle("🔨 | You have been kicked from " + event.getGuild().getName())
                    .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                    .setThumbnail(this.appConfig.embedSettings.errorEmbed.thumbnail)
                    .setDescription("Reason: " + reason)
                    .setTimestamp(Instant.now())
                    .build();

                channel.sendMessageEmbeds(embed).submit();
            });

            MessageEmbed embed = new EmbedBuilder()
                .setTitle("✅ | Successfully kicked " + user.getName())
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                .setDescription("Reason: " + reason)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

            event.replyEmbeds(embed)
                .setEphemeral(true)
                .queue();

            event.getGuild().kick(user).reason(reason).queue();
        }
        catch (Exception exception) {
            MessageEmbed embed = new EmbedBuilder()
                .setTitle("❌ | An error occurred while kicking the user")
                .setColor(Color.decode(this.appConfig.embedSettings.errorEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.errorEmbed.thumbnail)
                .setDescription("I can't kick this user, he probably has highest role than me!")
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

            event.replyEmbeds(embed)
                .setEphemeral(true)
                .queue();
        }
    }
}
