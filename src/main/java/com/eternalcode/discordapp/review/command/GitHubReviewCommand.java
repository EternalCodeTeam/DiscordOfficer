package com.eternalcode.discordapp.review.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.command.subcommands.AddChild;
import com.eternalcode.discordapp.review.command.subcommands.ListChild;
import com.eternalcode.discordapp.review.command.subcommands.RemoveChild;
import com.eternalcode.discordapp.review.command.subcommands.RequestChild;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public class GitHubReviewCommand extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public GitHubReviewCommand(GitHubReviewService gitHubReviewService) {
        this.name = "review";
        this.help = "Review a GitHub pull request";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.gitHubReviewService = gitHubReviewService;

        this.children = new SlashCommand[]{
                new AddChild(gitHubReviewService),
                new ListChild(gitHubReviewService),
                new RemoveChild(gitHubReviewService),
                new RequestChild(gitHubReviewService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {}
}
