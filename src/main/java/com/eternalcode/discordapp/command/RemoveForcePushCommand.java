package com.eternalcode.discordapp.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.OffsetDateTime;
import java.util.List;

public class RemoveForcePushCommand extends SlashCommand {

    public RemoveForcePushCommand() {
        this.name = "removeforcepush";
        this.help = "Removes a force push from the specified channel";

        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.CHANNEL, "channel", "The channel to remove the force push")
                        .setRequired(true),
                new OptionData(OptionType.INTEGER, "days", "The amount of days to remove the force push")
                        .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        MessageChannel channel = event.getOption("channel").getAsChannel().asTextChannel();
        int days = event.getOption("days").getAsInt();

        try {
            deleteForcePushedMessages(channel, days);
            event.reply("Successfully removed force pushed messages")
                    .setEphemeral(true)
                    .queue();
        }
        catch (Exception exception) {
            event.reply("Failed to remove force pushed messages")
                    .setEphemeral(true)
                    .queue();
        }
    }

    public void deleteForcePushedMessages(MessageChannel channel, int days) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime beforeDays = now.minusDays(days);

        channel.getIterableHistory()
                .takeAsync(100)
                .thenApply(messages -> messages.stream()
                        .filter(message -> message.getTimeCreated().isAfter(beforeDays))
                        .filter(message -> message.getEmbeds().stream()
                                .anyMatch(embed -> embed.getTitle().contains("force-pushed")))
                )
                .thenAccept(detectedMessages -> detectedMessages.forEach(message -> message.delete().queue()));
    }


}
