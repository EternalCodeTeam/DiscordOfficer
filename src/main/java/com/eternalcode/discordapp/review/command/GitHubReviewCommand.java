package com.eternalcode.discordapp.review.command;

import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.review.GitHubReviewService;
import com.eternalcode.discordapp.review.command.child.AddChild;
import com.eternalcode.discordapp.review.command.child.ForumTagsIdChild;
import com.eternalcode.discordapp.review.command.child.ListChild;
import com.eternalcode.discordapp.review.command.child.NotificationChild;
import com.eternalcode.discordapp.review.command.child.RemoveChild;
import com.eternalcode.discordapp.review.command.child.RequestChild;
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
                new NotificationChild(gitHubReviewService)
        };
    }

    @Override
    public void execute(SlashCommandEvent event) {
        /* This method is empty because uses children for sub-commands. */
    }
}
