package com.eternalcode.discordapp.feature.review.command.child;

import com.eternalcode.discordapp.feature.review.GitHubReviewService;
import com.eternalcode.discordapp.feature.review.GitHubReviewUser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class ListChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public ListChild(GitHubReviewService gitHubReviewService) {
        this.name = "list";
        this.help = "List all users in review system";

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            List<GitHubReviewUser> listOfUsers = this.gitHubReviewService.getListOfUsers();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            for (GitHubReviewUser user : listOfUsers) {
                embedBuilder.addField(
                    user.getGithubUsername(),
                    "Discord ID: " + user.getDiscordId() + "\nNotification Type: " + user.getNotificationType(),
                    false
                );
            }

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while listing the users").setEphemeral(true).queue();
            Sentry.captureException(exception);
            exception.printStackTrace();
        }
    }
}
