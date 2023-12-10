package com.eternalcode.discordapp.meeting.controller;

import com.eternalcode.discordapp.meeting.database.MeetingRepository;
import com.eternalcode.discordapp.meeting.database.MeetingRepositoryImpl;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MeetingButtonController extends ListenerAdapter {

    private final MeetingRepository meetingRepository;

    public MeetingButtonController(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

    }
}

