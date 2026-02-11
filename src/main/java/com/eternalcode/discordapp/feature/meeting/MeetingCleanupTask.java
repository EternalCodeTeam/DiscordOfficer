package com.eternalcode.discordapp.feature.meeting;

import java.time.Instant;
import net.dv8tion.jda.api.JDA;

public class MeetingCleanupTask implements Runnable {

    private final MeetingService meetingService;
    private final JDA jda;

    public MeetingCleanupTask(MeetingService meetingService, JDA jda) {
        this.meetingService = meetingService;
        this.jda = jda;
    }

    @Override
    public void run() {
        this.meetingService.cleanupExpired(jda, Instant.now());
    }
}
