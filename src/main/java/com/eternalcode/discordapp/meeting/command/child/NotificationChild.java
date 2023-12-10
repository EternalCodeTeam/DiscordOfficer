package com.eternalcode.discordapp.meeting.command.child;

import com.eternalcode.discordapp.meeting.MeetingNotificationType;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class NotificationChild extends SlashCommand {

    public NotificationChild() {
        this.name = "notification";

        this.options = List.of(
            new OptionData(OptionType.STRING, "notification-type", "type of notification send by review system")
                .addChoice("DM", MeetingNotificationType.DM.toString())
                .addChoice("SERVER", MeetingNotificationType.SERVER.toString())
                .addChoice("BOTH", MeetingNotificationType.BOTH.toString())
                .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent slashCommandEvent) {
        String notificationTypeString = slashCommandEvent.getOption("notification-type").getAsString();
        MeetingNotificationType notificationType = MeetingNotificationType.valueOf(notificationTypeString);
    }
}
