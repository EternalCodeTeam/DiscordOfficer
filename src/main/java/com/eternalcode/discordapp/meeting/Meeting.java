package com.eternalcode.discordapp.meeting;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Meeting {

    private Set<Long> presentMembers = new HashSet<>();
    private Set<Long> absentMembers = new HashSet<>();

    private Long requesterId;

    private Instant issuedAt;
    private Instant startTime;

    public Meeting(Long requesterId, Instant issuedAt, Instant startTime) {
        this.requesterId = requesterId;
        this.issuedAt = issuedAt;
        this.startTime = startTime;
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
