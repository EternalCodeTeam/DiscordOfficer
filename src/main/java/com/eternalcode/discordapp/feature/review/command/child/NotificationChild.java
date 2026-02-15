package com.eternalcode.discordapp.feature.review.command.child;

import com.eternalcode.discordapp.feature.review.GitHubReviewNotificationType;
import com.eternalcode.discordapp.feature.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class NotificationChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public NotificationChild(GitHubReviewService gitHubReviewService) {
        this.name = "notification";

        this.options = List.of(
            new OptionData(OptionType.STRING, "notification-type", "type of notification send by review system")
                .addChoice("DM", GitHubReviewNotificationType.DM.toString())
                .addChoice("SERVER", GitHubReviewNotificationType.SERVER.toString())
                .addChoice("BOTH", GitHubReviewNotificationType.BOTH.toString())
                .setRequired(true)
        );

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String notificationTypeString = event.getOption("notification-type").getAsString();
        GitHubReviewNotificationType notificationType = GitHubReviewNotificationType.valueOf(notificationTypeString);

        this.gitHubReviewService.updateUserNotificationType(event.getUser().getIdLong(), notificationType);
        event.reply("Notification type updated").setEphemeral(true).queue();
    }
}

