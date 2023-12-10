package com.eternalcode.discordapp.meeting.event;

import com.eternalcode.discordapp.meeting.Meeting;

public record MeetingCreateEvent(Meeting meeting, Long requester, Long channelId) {
}
