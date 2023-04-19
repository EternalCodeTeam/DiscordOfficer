package com.eternalcode.discordapp.filter;

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
            if (embed == null) {
                return;
            }

            if (embed.getAuthor() == null) {
                return;
            }

            String name = embed.getAuthor().getName();
            String title = embed.getTitle();

            if (name == null || title == null) {
                return;
            }

            FilterResult result = this.filterService.check(name, title);

            if (!result.isPassed()) {
                event.getMessage().delete().queue();
            }
        });
    }
}
