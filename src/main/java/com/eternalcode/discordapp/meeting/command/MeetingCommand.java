package com.eternalcode.discordapp.meeting.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.meeting.MeetingService;
import com.eternalcode.discordapp.meeting.command.child.CreateChild;
import com.eternalcode.discordapp.meeting.command.child.NotificationChild;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class MeetingCommand extends SlashCommand {

    private final AppConfig appConfig;
    private final MeetingService meetingService;

    public MeetingCommand(AppConfig appConfig, MeetingService meetingService) {
        this.appConfig = appConfig;
        this.meetingService = meetingService;

        this.name = "meeting";
        this.help = "Sheduling meetings and reminds team members about it";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.children = new SlashCommand[]{
                new CreateChild(this.appConfig, this.meetingService),
                new NotificationChild()
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {

        MessageEmbed build = new EmbedBuilder()
                .setTitle("📅 | Dzisiaj spotkanie!")
                .setColor(Color.decode(this.appConfig.embedSettings.successEmbed.color))
                .setThumbnail(this.appConfig.embedSettings.successEmbed.thumbnail)
                .addField("Będę obecny", "", true)
                .addField("Nie będzie mnie", "", true)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .build();
    }
}
