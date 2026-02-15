package com.eternalcode.discordapp.feature.review.command.child;

import com.eternalcode.discordapp.feature.review.GitHubReviewService;
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

public class RemoveChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveChild.class);
    private final GitHubReviewService gitHubReviewService;

    public RemoveChild(GitHubReviewService gitHubReviewService) {
        this.name = "remove";
        this.help = "Remove a user from the review system";

        this.userPermissions = new Permission[]{ Permission.ADMINISTRATOR };

        this.options = List.of(
            new OptionData(OptionType.USER, "user", "User to remove from review system")
                .setRequired(true)
        );

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            OptionMapping userOption = event.getOption("user");
            if (userOption == null) {
                event.reply("Missing required option: user").setEphemeral(true).queue();
                return;
            }

            long discordId = userOption.getAsUser().getIdLong();

            boolean userFromSystem = this.gitHubReviewService.removeUserFromSystem(discordId);
            String message = userFromSystem ? "User removed" : "User does not exist, nothing to remove";
            event.reply(message).setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while removing the user from the system").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to remove user from review system", exception);
        }
    }

}

