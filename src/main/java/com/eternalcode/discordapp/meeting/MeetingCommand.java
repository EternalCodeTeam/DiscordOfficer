package com.eternalcode.discordapp.meeting;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.time.Instant;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class MeetingCommand extends SlashCommand {

    private final MeetingService meetingService;

    public MeetingCommand(MeetingService meetingService) {
        this.meetingService = meetingService;

        this.name = "meeting";
        this.help = "Tworzy Spotkaniomierz — embed z przyciskami do potwierdzania obecności";
        this.userPermissions = new Permission[] {Permission.ADMINISTRATOR };

        this.options = List.of(
            new OptionData(OptionType.STRING, "topic", "Temat/Opis spotkania (np. Poniedziałek 19:00 — weekly sync)")
                .setRequired(true),
            new OptionData(
                OptionType.INTEGER, "when", "Discordowy timestamp (sekundy UNIX), np. 1758394800 "
                + "(Generator - https://www.hammertime.cyou/pl)")
                .setRequired(true)
        );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        if (event.getGuild() == null) {
            event.reply("Ta komenda działa tylko na serwerze.").setEphemeral(true).queue();
            return;
        }

        String topic = event.getOption("topic").getAsString();
        long epochSeconds = event.getOption("when").getAsLong();

        if (epochSeconds <= 0) {
            event.reply("Podaj poprawny discordowy timestamp UNIX (musi być większy od 0.)").setEphemeral(true).queue();
            return;
        }

        Instant meetingAt = Instant.ofEpochSecond(epochSeconds);
        this.meetingService.createMeeting(event.getChannel(), event.getGuild().getIdLong(), topic, meetingAt);

        event.reply("Utworzono Spotkaniomierz dla: " + topic)
            .setEphemeral(true)
            .queue();
    }
}
