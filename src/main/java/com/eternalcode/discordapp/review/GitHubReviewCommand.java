package com.eternalcode.discordapp.review;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
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

        boolean pullRequestUrl = GitHubReviewUtil.isPullRequestUrl(url);

        if (!pullRequestUrl) {
            event.reply("Invalid GitHub pull request URL").setEphemeral(true).queue();
            return;
        }

        try {
            boolean checkPullRequestTitle = this.gitHubReviewService.checkPullRequestTitle(url);

            if (!checkPullRequestTitle) {
                event.reply("Invalid pull request title, use GH-NUMBER convention").setEphemeral(true).queue();
                return;
            }
        }
        catch (IOException exception) {
            event.reply("Failed to check pull request title").setEphemeral(true).queue();
            return;
        }

        try {
            long channelWithPRTitleAndMention = this.gitHubReviewService.createChannelWithPRTitleAndMention(event.getGuild(), url);
            System.out.println(channelWithPRTitleAndMention);
            this.gitHubReviewService.mentionReviewers(event, url, channelWithPRTitleAndMention);

            event.reply(String.format("Review started <#%s>", channelWithPRTitleAndMention)).setEphemeral(true).queue();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }

}
