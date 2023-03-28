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
/*        event.getMessage().getEmbeds().forEach(embed -> {
            FilterResult result = this.filterService.check(embed.getAuthor().getName(), embed.getTitle());

            if (!result.isPassed()) {
                event.getMessage().delete().queue();
            }
        });*/
    }
}
