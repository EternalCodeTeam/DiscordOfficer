package com.eternalcode.discordapp.feature.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public class AvatarCommand extends SlashCommand {

    private final AppConfig appConfig;

    public AvatarCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "avatar";
        this.help = "Shows the avatar of a user";

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "select the user")
                        .setRequired(false)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        User user = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("🖼 | " + user.getName() + "'s avatar")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setImage(user.getEffectiveAvatarUrl() + "?size=2048")
                .setTimestamp(Instant.now())
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .build();

        event.replyEmbeds(embed)
                .setEphemeral(true)
                .queue();
    }
}

