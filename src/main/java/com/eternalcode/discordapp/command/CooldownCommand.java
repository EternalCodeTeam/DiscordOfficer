package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public class CooldownCommand extends SlashCommand {

    private final AppConfig appConfig;

    public CooldownCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "cooldown";
        this.help = "Sets the cooldown of a chat channel";
        this.userPermissions = new Permission[]{ Permission.MANAGE_CHANNEL };
        this.options = List.of(
                new OptionData(OptionType.INTEGER, "cooldown", "select the cooldown")
                        .setRequired(true)
                        .setMinValue(1)
                        .setMaxValue(21600)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        int cooldown = event.getOption("cooldown").getAsInt();

        event.getChannel().asTextChannel().getManager().setSlowmode(cooldown).queue();

        MessageEmbed embeds = new EmbedBuilder()
                .setTitle("✅ | Success!")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                .setDescription("This channel's cooldown is now " + cooldown + " seconds")
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}