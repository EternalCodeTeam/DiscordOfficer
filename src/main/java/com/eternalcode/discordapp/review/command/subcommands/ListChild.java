package com.eternalcode.discordapp.review.command.subcommands;

import com.eternalcode.discordapp.review.GitHubReviewService;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Map;

public class ListChild extends SlashCommand {

    private final GitHubReviewService gitHubReviewService;

    public ListChild(GitHubReviewService gitHubReviewService) {
        this.name = "list";
        this.help = "List all users in review system";

        this.gitHubReviewService = gitHubReviewService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            List<Map.Entry<String, Long>> listOfUsers = this.gitHubReviewService.getListOfUsers();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            for (Map.Entry<String, Long> user : listOfUsers) {
                embedBuilder.addField(user.getKey(), String.valueOf(user.getValue()), false);
            }

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
        catch (Exception exception) {
            event.reply("An error occurred while listing the users").setEphemeral(true).queue();
        }
    }
}