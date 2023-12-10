package com.eternalcode.discordapp.meeting;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.config.ConfigManager;
import com.eternalcode.discordapp.meeting.command.child.CreateChild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;

public class MeetingService {

    private final AppConfig appConfig;
    private final ConfigManager configManager;
    private final CreateChild createChild;

    public MeetingService(AppConfig appConfig, ConfigManager configManager, CreateChild createChild) {
        this.appConfig = appConfig;
        this.configManager = configManager;
        this.createChild = createChild;
    }

    public void createMeeting(String title, String description, String dateTime, Member requester, Member chairperson, Channel announcementChannel) {
        ArrayList<Member> presentMembers = new ArrayList<>();
        ArrayList<Member> absentMembers = new ArrayList<>();

        MessageEmbed embed = new EmbedBuilder()
            .setTitle("📅 | Meeting requested")
            .setColor(Color.decode(this.appConfig.embedSettings.meetingEmbed.color))
            .setThumbnail(this.appConfig.embedSettings.meetingEmbed.thumbnail)
            .setDescription("")
            .addField("Present", "", true)
            .addField("Absent", "", true)
            .setFooter("Requested by " + requester.getUser().getName(), requester.getAvatarUrl())
            .setTimestamp(Instant.now())
            .build();
    }
}
