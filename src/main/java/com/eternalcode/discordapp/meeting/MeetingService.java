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
        Meeting meeting = new Meeting(requester, issuedAt, startTime, null, null);

        this.meetingRepository.saveMeeting(meeting);
        this.observerRegistry.publish(new MeetingCreateEvent(meeting, requester, channelId));
    }

    // TODO; tutaj sie zrobi metode od update i create embed, a z obiektu z bazy bedzie sie pobierac absent i present osoby
    // TODO: i bedzie sie je dodawac do embeda, a potem edytowac embed juz wczesniej wyslany za pomoca metody od update
    // TODO: i sa eventy MeetinbMemberAbsentEvent ii MeetingMemberPresent i bedzie sie je nasluchiwac i dodawac do listy albo present albo absent
    // TODO: ten kod co napisalem juz to troche sensu w niektorych momentach nie ma, ale ogolnie to jest to co trzeba zrobic ^^, jak ktos chce to niech zrobi

}
