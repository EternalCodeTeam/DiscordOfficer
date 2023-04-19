package com.eternalcode.discordapp.review;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class GitHubReviewCommand extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public GitHubReviewCommand(GitHubReviewService gitHubReviewService) {
        this.name = "review";
        this.help = "Review a GitHub pull request";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.options = List.of(
                new OptionData(OptionType.STRING, "url", "Review a GitHub pull request")
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
