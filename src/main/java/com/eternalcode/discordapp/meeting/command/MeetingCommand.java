package com.eternalcode.discordapp.meeting.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.meeting.MeetingService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MeetingCommand extends SlashCommand {

    private final AppConfig appConfig;
    private final MeetingService meetingService;

    public MeetingCommand(AppConfig appConfig, MeetingService meetingService) {
        this.appConfig = appConfig;
        this.meetingService = meetingService;

        this.name = "meeting";
        this.help = "Sheduling meetings and reminds team members about it";
        this.userPermissions = new Permission[]{ Permission.ADMINISTRATOR };

        this.options = List.of(
            new OptionData(OptionType.STRING, "time", "time of the meeting e.g: 21:00")
                .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String time = event.getOption("time") != null ? event.getOption("time").getAsString() : null;

        if (time == null) {
            event.reply("You need to specify time of the meeting e.g: 21:00")
                .setEphemeral(true)
                .queue();
            return;
        }

        LocalTime localTime;
        try {
            localTime = LocalTime.parse(time);
        }
        catch (DateTimeParseException exception) {
            event.reply("Invalid time format, you can use 24h format e.g: 21:00, 12:00, 21:30 etc.")
                .setEphemeral(true)
                .queue();
            return;
        }

        Instant meetingTime = localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();

        if (meetingTime.isBefore(Instant.now())) {
            event.reply("Meeting time cannot be in the past")
                .setEphemeral(true)
                .queue();
            return;
        }

        this.meetingService.createMeeting(Instant.now(), meetingTime, event.getUser().getIdLong(), event.getChannel().getIdLong());

        event.reply("Meeting created")
            .setEphemeral(true)
            .queue();
    }
}
