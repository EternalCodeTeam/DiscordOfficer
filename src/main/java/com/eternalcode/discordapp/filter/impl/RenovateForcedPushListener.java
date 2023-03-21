package com.eternalcode.discordapp.filter.impl;

import com.eternalcode.discordapp.filter.FilterService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RenovateForcedPushListener extends ListenerAdapter {

    private final FilterService filterService;

    public RenovateForcedPushListener(FilterService filterService) {
        this.filterService = filterService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        event.getMessage().getEmbeds().forEach(messageEmbed -> {
            String embedAndAuthor = messageEmbed.getAuthor().getName() + messageEmbed.getTitle();
            boolean isPossible = this.filterService.filterSource(embedAndAuthor);

            if (isPossible) {
                event.getMessage().delete().queue();
            }
        });
    }
}
