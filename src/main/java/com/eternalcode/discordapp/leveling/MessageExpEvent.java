package com.eternalcode.discordapp.leveling;

import com.eternalcode.discordapp.database.model.UserPoints;
import com.eternalcode.discordapp.database.repository.userpoints.UserPointsRepositoryImpl;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class MessageExpEvent extends ListenerAdapter {
    private final UserPointsRepositoryImpl userPointsRepository;
    private static int HOW_MANY_WORDS_TO_GIVE_POINTS = 5;

    public MessageExpEvent(UserPointsRepositoryImpl userPointsRepository) {
        this.userPointsRepository = userPointsRepository;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        this.givePoints(event);
    }

    private void givePoints(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");

        if (message.length < HOW_MANY_WORDS_TO_GIVE_POINTS) {
            return;
        }

        UserPoints user = null;

        try {
            user = this.userPointsRepository.findUser(event.getAuthor().getIdLong()).get();
        }
        catch (Exception exception) {
            user = new UserPoints(event.getAuthor().getIdLong(), 0);
        }

        int points = (Math.round(message.length / HOW_MANY_WORDS_TO_GIVE_POINTS)) * 10;
        user.addPoints(points);
        this.userPointsRepository.saveUser(user).get();
    }

}
