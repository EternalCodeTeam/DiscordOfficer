package com.eternalcode.discordapp.ticket;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.Instant;

@DatabaseTable(tableName = "tickets")
public class TicketWrapper {

    @DatabaseField(generatedId = true, columnName = "id")
    private long id;

    @DatabaseField(columnName = "user_id", index = true)
    private long userId;

    @DatabaseField(columnName = "channel_id", index = true)
    private long channelId;

    @DatabaseField(columnName = "category")
    private String category;

    @DatabaseField(columnName = "created_at")
    private long createdAt;

    public TicketWrapper() {
    }

    public TicketWrapper(long id, long userId, long channelId, String categoryId) {
        this.id = id;
        this.userId = userId;
        this.channelId = channelId;
        this.category = categoryId;
        this.createdAt = Instant.now().getEpochSecond();
    }

    public long getId() {
        return this.id;
    }

    public long getUserId() {
        return this.userId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public String getCategory() {
        return this.category;
    }

    public Instant getCreatedAt() {
        return Instant.ofEpochSecond(this.createdAt);
    }
}
