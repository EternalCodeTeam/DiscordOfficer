package com.eternalcode.discordapp.feature.meeting;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.Instant;

@DatabaseTable(tableName = "meeting_poll")
public class MeetingPollWrapper {

    @DatabaseField(id = true, columnName = "message_id")
    private long messageId;

    @DatabaseField(columnName = "guild_id", index = true)
    private long guildId;

    @DatabaseField(columnName = "channel_id", index = true)
    private long channelId;

    @DatabaseField(columnName = "topic")
    private String topic;

    @DatabaseField(columnName = "created_at")
    private long createdAt;

    @DatabaseField(columnName = "meeting_at")
    private long meetingAt;

    public MeetingPollWrapper() {
    }

    public MeetingPollWrapper(
        long messageId,
        long guildId,
        long channelId,
        String topic,
        Instant createdAt,
        Instant meetingAt
    ) {
        this.messageId = messageId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.topic = topic;
        this.createdAt = createdAt.getEpochSecond();
        this.meetingAt = meetingAt.getEpochSecond();
    }

    public long getMessageId() {
        return messageId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getTopic() {
        return topic;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getMeetingAt() {
        return meetingAt;
    }
}
