package com.eternalcode.discordapp.review.command.child;

import com.eternalcode.discordapp.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class RequestChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public RequestChild(GitHubReviewService gitHubReviewService) {
        this.name = "request";
        this.help = "Request a review";

        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.STRING, "url", "The URL of the pull request")
                        .setRequired(true)
        );

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        String url = event.getOption("url").getAsString();

        String review = this.gitHubReviewService.createReview(event.getGuild(), url, event.getJDA());
        event.reply(review).setEphemeral(true).queue();
    }
}
