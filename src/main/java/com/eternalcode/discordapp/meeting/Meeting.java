package com.eternalcode.discordapp.meeting;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Meeting {

    private Instant startTime;
    private final Set<Long> presentMembers = new HashSet<>();
    private final Set<Long> absentMembers = new HashSet<>();

    void addPresentMemeber(long id) {
        this.presentMembers.add(id);
    }

}
