package com.eternalcode.discordapp.review.command.child;

import com.eternalcode.discordapp.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class AddChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public AddChild(GitHubReviewService gitHubReviewService) {
        this.name = "add";
        this.help = "Add user to review system";

        this.userPermissions = new Permission[]{ Permission.ADMINISTRATOR };

        this.options = List.of(
                new OptionData(OptionType.USER, "user", "Add user to reviewers list")
                        .setRequired(true),
                new OptionData(OptionType.STRING, "github-username", "GitHub username of the user")
                        .setRequired(true)
        );

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            Long discordUsername = event.getOption("user").getAsUser().getIdLong();
            String githubUsername = event.getOption("github-username").getAsString();

            this.gitHubReviewService.addUserToSystem(discordUsername, githubUsername);
            event.reply("User added to the system").setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while adding user to the system").setEphemeral(true).queue();
        }
    }
}