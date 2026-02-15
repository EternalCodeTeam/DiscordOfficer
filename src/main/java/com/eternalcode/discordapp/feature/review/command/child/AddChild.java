package com.eternalcode.discordapp.feature.review.command.child;

import com.eternalcode.discordapp.feature.review.GitHubReviewNotificationType;
import com.eternalcode.discordapp.feature.review.GitHubReviewService;
import com.eternalcode.discordapp.feature.review.GitHubReviewUser;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddChild.class);
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
            OptionMapping userOption = event.getOption("user");
            OptionMapping githubOption = event.getOption("github-username");
            if (userOption == null || githubOption == null) {
                event.reply("Missing required options: user and github-username").setEphemeral(true).queue();
                return;
            }

            long discordUsername = userOption.getAsUser().getIdLong();
            String githubUsername = githubOption.getAsString().trim();
            if (githubUsername.isEmpty()) {
                event.reply("GitHub username cannot be empty").setEphemeral(true).queue();
                return;
            }

            GitHubReviewUser gitHubReviewUser = new GitHubReviewUser(discordUsername, githubUsername, GitHubReviewNotificationType.BOTH);

            boolean addUserToSystem = this.gitHubReviewService.addUserToSystem(gitHubReviewUser);
            String message = addUserToSystem ? "User added" : "User already exists";
            event.reply(message).setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while adding user to the system").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to add user to review system", exception);
        }
    }
}

