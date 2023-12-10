package com.eternalcode.discordapp.meeting.database;

import com.eternalcode.discordapp.meeting.Meeting;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.Instant;

@DatabaseTable(tableName = "officer_meetings")
class MeetingWrapper {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField(dataType = DataType.LONG)
    private long requesterId;

    @DatabaseField(columnName = "issuedAt", dataType = DataType.SERIALIZABLE)
    private Instant issuedAt;

    @DatabaseField(columnName = "startTime", dataType = DataType.SERIALIZABLE)
    private Instant startTime;
/*
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private final Set<Long> presentMembers = new HashSet<>();

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private final Set<Long> absentMembers = new HashSet<>();*/

    public MeetingWrapper(Instant issuedAt, Instant startTime) {
        this.issuedAt = issuedAt;
        this.startTime = startTime;
    }

    public MeetingWrapper() {
    }

    public static MeetingWrapper from(Meeting meeting) {
        return new MeetingWrapper(meeting.getIssuedAt(), meeting.getStartTime());
    }

    public Meeting toMeeting() {
        return new Meeting(this.requesterId, this.issuedAt, this.startTime);
    }

}
