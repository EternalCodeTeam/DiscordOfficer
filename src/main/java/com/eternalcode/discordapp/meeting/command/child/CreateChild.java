package com.eternalcode.discordapp.meeting.command.child;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.meeting.MeetingService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.List;

public class CreateChild extends SlashCommand {

    private final AppConfig appConfig;
    private final MeetingService meetingService;

    public CreateChild(AppConfig appConfig, MeetingService meetingService) {
        this.appConfig = appConfig;
        this.meetingService = meetingService;

        this.name = "create";
        this.help = "Create meeting";
        this.userPermissions = new Permission[]{ Permission.ADMINISTRATOR };

        this.options = List.of(
            new OptionData(OptionType.CHANNEL, "channel", "Notification channel")
                .setRequired(false),

            new OptionData(OptionType.USER, "chairperson", "Chairperson of meeting")
                .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.replyModal(this.getModal()).queue();

        Member chairperson = event.getOption("chairperson").getAsMember();
        Channel announcementChannel = event.getOption("channel").getAsChannel();

        this.meetingService.createMeeting(this.getSubject(), this.getDescription(), this.getDateTime(), (Member) event.getUser(), chairperson, announcementChannel);
    }

    Modal getModal() {
        TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
            .setPlaceholder("Subject of meeting")
            .setRequiredRange(1, 100)
            .setRequired(true)
            .build();

        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Description of meeting")
            .setRequiredRange(30, 1000)
            .setRequired(false)
            .build();

        TextInput dateTime = TextInput.create("dateTime", "Date and time", TextInputStyle.SHORT)
            .setPlaceholder("01/01/2024 19:00")
            .setRequiredRange(16, 16)
            .setRequired(true)
            .build();

        return Modal.create("meetingModal", "Create meeting")
            .addComponents(ActionRow.of(subject), ActionRow.of(description), ActionRow.of(dateTime))
            .build();
    }

    public String getSubject() {
        return this.getModal().getComponents().get(1).toString();
    }

    public String getDescription() {
        return this.getModal().getComponents().get(2).toString();
    }

    public String getDateTime() {
        return this.getModal().getComponents().get(3).toString();
    }
}
