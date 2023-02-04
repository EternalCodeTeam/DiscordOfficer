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
import java.util.List;

public class KickCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public KickCommand(DiscordAppConfig discordAppConfig) {
        this.name = "kick";
        this.help = "Kicks a user";
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "select the user").setRequired(false),
                new OptionData(OptionType.STRING, "reason", "provide a reason").setRequired(false)
        );

        this.discordAppConfig = discordAppConfig;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            User user = event.getOption("user").getAsUser();

            if (user.getId().equalsIgnoreCase(event.getUser().getId())) {
                event.reply("You can't kick yourself")
                        .setEphemeral(true)
                        .queue();

                return;
            }

            String kickReason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

            user.openPrivateChannel().queue(privateChannel -> {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("🔨 | You have been kicked from " + event.getGuild().getName())
                        .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                        .setDescription("Reason: " + kickReason)
                        .build();

                privateChannel.sendMessageEmbeds(embed).queue();
            });

            MessageEmbed build = new EmbedBuilder()
                    .setTitle("✅ | Successfully kicked " + user.getAsTag())
                    .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                    .setDescription("Reason: " + kickReason)
                    .build();

            event.replyEmbeds(build)
                    .setEphemeral(true)
                    .queue();

            event.getGuild().kick(user).reason(kickReason).queue();
        } catch (Exception ignored) {

        }
    }

}