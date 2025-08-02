package com.eternalcode.discordapp.review.command.child;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RequestChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public RequestChild(GitHubReviewService gitHubReviewService) {
        this.name = "request";
        this.help = "Request a review";

        this.userPermissions = new Permission[] {Permission.MESSAGE_MANAGE};

        this.options = List.of(
            new OptionData(OptionType.STRING, "url", "The URL of the pull request")
                .setRequired(true)
        );

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String url = event.getOption("url").getAsString();

        this.gitHubReviewService.createReview(event.getGuild(), url, event.getJDA())
            .thenAccept(message -> event.reply(message)
                .setEphemeral(true)
                .queue())
            .exceptionally(FutureHandler::handleException);
    }
}
