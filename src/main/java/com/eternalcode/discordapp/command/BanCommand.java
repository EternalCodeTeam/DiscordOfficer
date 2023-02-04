package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.Embeds;
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
import java.util.concurrent.TimeUnit;

public class BanCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public BanCommand(DiscordAppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;

        this.name = "ban";
        this.help = "Ban a user from the server";
        this.userPermissions = new Permission[] { Permission.BAN_MEMBERS };
        this.options = List.of(
                new OptionData(OptionType.USER, "user", "user").setRequired(true),
                new OptionData(OptionType.INTEGER, "deletion-time", "deletion-time").setRequired(true),
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

            user.openPrivateChannel().queue(privateChannel -> {
                MessageEmbed embed = new Embeds().error
                        .setTitle("🔨 | You have been banned from " + event.getGuild().getName())
                        .setDescription("Reason: " + kickReason)
                        .build();

                privateChannel.sendMessageEmbeds(embed).queue();
            });

            MessageEmbed build = new EmbedBuilder()
                    .setTitle("✅ | Successfully banned " + user.getAsTag())
                    .setDescription("Reason: " + kickReason)
                    .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                    .build();

            event.replyEmbeds(build)
                    .setEphemeral(true)
                    .queue();

            event.getGuild().ban(user, deletionTime, TimeUnit.DAYS).reason(reason).queue();
        } catch (Exception ignored) {

        }
    }

}