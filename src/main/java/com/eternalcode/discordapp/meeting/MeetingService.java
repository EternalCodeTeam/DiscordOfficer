package com.eternalcode.discordapp.meeting;

import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.meeting.database.MeetingRepository;
import com.eternalcode.discordapp.meeting.database.MeetingRepositoryImpl;
import com.eternalcode.discordapp.meeting.event.MeetingCreateEvent;
import com.eternalcode.discordapp.observer.ObserverRegistry;

import java.time.Instant;

public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ObserverRegistry observerRegistry;

    public MeetingService(DatabaseManager databaseManager, ObserverRegistry observerRegistry) {
        this.meetingRepository = MeetingRepositoryImpl.create(databaseManager);
        this.observerRegistry = observerRegistry;
    }

    public void createMeeting(Instant issuedAt, Instant startTime, Long requester, Long channelId) {
        Meeting meeting = new Meeting(requester, issuedAt, startTime);

        this.meetingRepository.saveMeeting(meeting);
        this.observerRegistry.publish(new MeetingCreateEvent(meeting, requester, channelId));
    }

    public void deleteMeeting(Instant issuedAt, Instant startTime, Long requester) {
        Meeting meeting = new Meeting(requester, issuedAt, startTime);

        this.meetingRepository.deleteMeeting(meeting);
    }

    public void findMeeting(Instant issuedAt, Instant startTime, Long requester) {
        Meeting meeting = new Meeting(requester, issuedAt, startTime);

        this.meetingRepository.findMeeting(meeting);
    }

    //zw

}
