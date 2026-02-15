package com.eternalcode.discordapp.feature.filter;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class FilterMessageEmbedController extends ListenerAdapter {

    private final FilterService filterService;

    public FilterMessageEmbedController(FilterService filterService) {
        this.filterService = filterService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        event.getMessage().getEmbeds().forEach(embed -> {
            if (embed == null || embed.getAuthor() == null) {
                return;
            }

            String name = embed.getAuthor().getName();
            String title = embed.getTitle();

            if (name == null || title == null) {
                return;
            }

            if (!filterService.check(name, title).isPassed()) {
                event.getMessage().delete().queue();
            }
        });
    }
}
