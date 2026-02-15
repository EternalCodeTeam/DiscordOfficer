package com.eternalcode.discordapp.feature.meeting;

import com.eternalcode.discordapp.feature.meeting.MeetingService.MeetingButtonUpdater;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class MeetingButtonController extends ListenerAdapter {

    private final MeetingService meetingService;

    public MeetingButtonController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("meeting_")) {
            return;
        }

        long messageId = event.getMessage().getIdLong();
        long userId = event.getUser().getIdLong();

        MeetingStatus status = switch (id) {
            case "meeting_yes" -> MeetingStatus.YES;
            case "meeting_no" -> MeetingStatus.NO;
            case "meeting_maybe" -> MeetingStatus.MAYBE;
            default -> null;
        };

        if (status == null) {
            event.reply("Nieznany przycisk.").setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        MeetingButtonUpdater updater = new MeetingButtonUpdater() {
            @Override
            public void editMessage(MessageEditData newData) {
                event.getHook().editOriginal(newData).queue(
                    success -> {},
                    failure -> event.getMessage().editMessage(newData).queue()
                );
            }

            @Override
            public void replyEphemeral(String content) {
                event.getHook().sendMessage(content).setEphemeral(true).queue();
            }

            @Override
            public void acknowledgeEphemeral(String content) {
                event.getHook().sendMessage(content).setEphemeral(true).queue();
            }
        };

        this.meetingService.handleVote(
            messageId,
            userId,
            status,
            updater
        );
    }
}

