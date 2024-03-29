package com.eternalcode.discordapp.review.command.child;

import com.eternalcode.discordapp.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class RemoveChild extends SlashCommand {

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
            Long discordId = event.getOption("user").getAsUser().getIdLong();

            boolean userFromSystem = this.gitHubReviewService.removeUserFromSystem(discordId);

            if (userFromSystem) {
                event.reply("User does not exist, nothing to remove").setEphemeral(true).queue();
            }

            event.reply("User removed").setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while removing the user from the system").setEphemeral(true).queue();
            Sentry.captureException(exception);
            exception.printStackTrace();
        }
    }

}
