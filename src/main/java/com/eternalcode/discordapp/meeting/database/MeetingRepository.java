package com.eternalcode.discordapp.meeting.database;

import com.eternalcode.discordapp.meeting.Meeting;

import java.util.concurrent.CompletableFuture;

public interface MeetingRepository {

    CompletableFuture<Meeting> findMeeting(Meeting meeting);

    CompletableFuture<Meeting> saveMeeting(Meeting meeting);

    CompletableFuture<Meeting> deleteMeeting(Meeting meeting);

}
