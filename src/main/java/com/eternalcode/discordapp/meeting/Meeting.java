package com.eternalcode.discordapp.meeting;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Meeting {

    private final Set<Long> presentMembers = new HashSet<>();
    private final Set<Long> absentMembers = new HashSet<>();

    private final Long requesterId;

    private final Instant issuedAt;
    private final Instant startTime;

    public Meeting(Long requesterId, Instant issuedAt, Instant startTime, Set<Long> presentMembers, Set<Long> absentMembers) {
        this.requesterId = requesterId;
        this.issuedAt = issuedAt;
        this.startTime = startTime;
        this.presentMembers.addAll(presentMembers);
        this.absentMembers.addAll(absentMembers);
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Set<Long> getPresentMembers() {
        return presentMembers;
    }

    public Set<Long> getAbsentMembers() {
        return absentMembers;
    }
}
