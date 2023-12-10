package com.eternalcode.discordapp.meeting;

public enum MeetingNotificationType {

    DM,
    SERVER,
    BOTH;

    public boolean isDmNotify() {
        return this == DM || this == BOTH;
    }

    public boolean isServerNotify() {
        return this == SERVER || this == BOTH;
    }
}
