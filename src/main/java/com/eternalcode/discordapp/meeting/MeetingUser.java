package com.eternalcode.discordapp.meeting;

import net.dzikoysk.cdn.entity.Contextual;
import net.dzikoysk.cdn.entity.Exclude;

@Contextual
public class MeetingUser {

    private long discordId;
    private MeetingNotificationType notificationType;
    private Boolean isPresent;

    public MeetingUser(Long discordId, MeetingNotificationType notificationType, Boolean isPresent) {
        this.discordId = discordId;
        this.notificationType = notificationType;
        this.isPresent = isPresent;
    }

    public MeetingUser() {}

    public MeetingNotificationType getNotificationType() {
        return this.notificationType;
    }

    @Exclude
    public void setNotificationType(MeetingNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Long getDiscordId() {
        return this.discordId;
    }

    public Boolean getIsPresent() {
        return this.isPresent;
    }
}
