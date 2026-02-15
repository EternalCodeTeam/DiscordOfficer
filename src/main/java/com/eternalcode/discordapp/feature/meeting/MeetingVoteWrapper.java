package com.eternalcode.discordapp.feature.meeting;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "meeting_vote")
public class MeetingVoteWrapper {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(columnName = "message_id", index = true)
    private long messageId;

    @DatabaseField(columnName = "user_id", index = true)
    private long userId;

    @DatabaseField(columnName = "status")
    private String status;

    public MeetingVoteWrapper() {
    }

    public MeetingVoteWrapper(long messageId, long userId, MeetingStatus status) {
        this.id = messageId + ":" + userId;
        this.messageId = messageId;
        this.userId = userId;
        this.status = status.name();
    }

    public String getId() {
        return id;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getUserId() {
        return userId;
    }

    public MeetingStatus getStatus() {
        return MeetingStatus.valueOf(status);
    }
}

