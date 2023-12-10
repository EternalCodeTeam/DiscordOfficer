package com.eternalcode.discordapp.meeting;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.meeting.event.MeetingCreateEvent;
import com.eternalcode.discordapp.observer.Observer;
import com.eternalcode.discordapp.util.DiscordTagFormat;
import com.eternalcode.discordapp.util.InstantFormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class MeetingController implements Observer<MeetingCreateEvent> {

    private final JDA jda;
    private final AppConfig appConfig;

    public MeetingController(JDA jda, AppConfig appConfig) {
        this.jda = jda;
        this.appConfig = appConfig;
    }

    @Override
    public void update(MeetingCreateEvent event) {
        Meeting meeting = event.meeting();
        Long channelId = event.channelId();

        TextChannel textChannelById = this.jda.getTextChannelById(channelId);

        if (textChannelById == null) {
            return;
        }

        Long requesterId = meeting.getRequesterId();

        Member memberById = this.jda.getGuildById(this.appConfig.guildId)
            .getMemberById(requesterId);

        if (memberById == null) {
            return;
        }

        String requesterName = memberById.getEffectiveName();

        // TODO: add everyone ping at created meeting

        Instant startTime = meeting.getStartTime();
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(startTime);

        String timeFieldValue = String.format("%s (%s)",
            InstantFormatUtil.format(startTime),
            DiscordTagFormat.toDiscordWhen(startTime.atOffset(offset)));

        MessageEmbed embed = new EmbedBuilder()
            .setTitle("📅 | Meeting requested")
            .setColor(Color.decode(this.appConfig.embedSettings.meetingEmbed.color))
            .setThumbnail(this.appConfig.embedSettings.meetingEmbed.thumbnail)
            .setDescription("TODO: Some description, dodałbym wzmiankę o przewodniczącym spotkania")
            .addField("Meeting Leader:", requesterName, false)
            .addField("Time:", timeFieldValue, false)
            .addField("Present", "", true)
            .addField("Absent", "", true)
            .setTimestamp(meeting.getIssuedAt())
            .build();

        Button presentButton = Button.primary("meeting:present:" + meeting.getIssuedAt().toEpochMilli(), "Present");
        Button absentButton = Button.danger("meeting:absent:" + meeting.getIssuedAt().toEpochMilli(), "Absent");

        textChannelById
            .sendMessageEmbeds(embed)
            .addActionRow(presentButton, absentButton)
            .queue();
    }

}
