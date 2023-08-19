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

public class ClearCommand extends SlashCommand {

    private final AppConfig appConfig;

    public ClearCommand(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.name = "clear";
        this.help = "Clears a certain amount of messages in the chat.";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };
        this.options = List.of(
                new OptionData(OptionType.INTEGER, "amount", "The amount of messages to clear")
                        .setMinValue(1)
                        .setMaxValue(100)
                        .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        int amount = event.getOption("amount").getAsInt();

        event.getChannel().getIterableHistory().takeAsync(amount)
                .thenAcceptAsync(messages -> event.getChannel().purgeMessages(messages));

        MessageEmbed embeds = new EmbedBuilder()
                .setTitle("✅ | Success!")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                .setDescription("Cleared " + amount + " messages")
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}