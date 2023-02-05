package com.eternalcode.discordapp.command;

import com.eternalcode.discordapp.config.DiscordAppConfig;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;

public class ClearCommand extends SlashCommand {

    private final DiscordAppConfig discordAppConfig;

    public ClearCommand(DiscordAppConfig discordAppConfig) {
        this.discordAppConfig = discordAppConfig;

        this.name = "clear";
        this.help = "Clears a certain amount of messages in the chat.";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "amount", "The amount of messages to clear")
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
                .setColor(Color.decode(this.discordAppConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.discordAppConfig.embedSettings.successEmbed.thumbnail)
                .setDescription("Cleared " + amount + " messages")
                .setFooter("Requested by " + event.getUser().getAsTag(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();

        event.replyEmbeds(embeds)
                .setEphemeral(true)
                .queue();
    }
}