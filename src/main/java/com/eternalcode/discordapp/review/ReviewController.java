package com.eternalcode.discordapp.review;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateLockedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReviewController extends ListenerAdapter {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void onChannelUpdateLocked(ChannelUpdateLockedEvent event) {
        ThreadChannel threadChannel = (ThreadChannel) event.getChannel();

        if (!threadChannel.isLocked()) {
            return;
        }

        threadChannel.getName();

    }
    
}
