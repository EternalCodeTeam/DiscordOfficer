package com.eternalcode.discordapp.feature.ticket;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.Instant;

@DatabaseTable(tableName = "tickets")
public class TicketWrapper {

    static final String COLUMN_ID = "id";
    static final String COLUMN_USER_ID = "user_id";
    static final String COLUMN_CHANNEL_ID = "channel_id";
    static final String COLUMN_CATEGORY = "category";
    static final String COLUMN_CREATED_AT = "created_at";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;

    @DatabaseField(columnName = COLUMN_USER_ID, index = true)
    private long userId;

    @DatabaseField(columnName = COLUMN_CHANNEL_ID, index = true)
    private long channelId;

    @DatabaseField(columnName = COLUMN_CATEGORY)
    private String category;

    @DatabaseField(columnName = COLUMN_CREATED_AT)
    private long createdAt;

    public TicketWrapper() {
    }

    public TicketWrapper(long userId, long channelId, String category) {
        this.userId = userId;
        this.channelId = channelId;
        this.category = category;
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
