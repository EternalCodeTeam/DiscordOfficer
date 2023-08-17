package com.eternalcode.discordapp.review.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.command.child.*;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

public class GitHubReviewCommand extends SlashCommand {

    public GitHubReviewCommand(GitHubReviewService gitHubReviewService, AppConfig appConfig) {
        this.name = "review";
        this.help = "Review a GitHub pull request";
        this.userPermissions = new Permission[]{ Permission.MESSAGE_MANAGE };

        this.children = new SlashCommand[]{
                new AddChild(gitHubReviewService),
                new ListChild(gitHubReviewService),
                new RemoveChild(gitHubReviewService),
                new RequestChild(gitHubReviewService),
                new ForumTagsIdChild(appConfig),
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {}
}
