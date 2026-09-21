package com.eternalcode.discordapp.feature.releasewatch.command.child;

import com.eternalcode.discordapp.feature.releasewatch.ReleaseWatchService;
import com.eternalcode.discordapp.feature.releasewatch.command.ReleaseCommandOptions;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchAddChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchAddChild.class);
    private final ReleaseWatchService releaseWatchService;

    public WatchAddChild(ReleaseWatchService releaseWatchService) {
        this.name = "watch-add";
        this.help = "Start watching a GitHub repository for new releases";

        this.userPermissions = new Permission[]{ Permission.MANAGE_SERVER };

        this.options = List.of(
            new OptionData(OptionType.STRING, ReleaseCommandOptions.REPO, "GitHub repository in owner/repo format")
                .setRequired(true),
            new OptionData(OptionType.STRING, ReleaseCommandOptions.NAME, "Display name shown in the release embed")
                .setRequired(true)
        );

        this.releaseWatchService = releaseWatchService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            OptionMapping repoOption = event.getOption(ReleaseCommandOptions.REPO);
            OptionMapping nameOption = event.getOption(ReleaseCommandOptions.NAME);
            if (repoOption == null || nameOption == null) {
                event.reply("Missing required options: repo and name").setEphemeral(true).queue();
                return;
            }

            event.deferReply(true).queue();

            this.releaseWatchService.addWatch(repoOption.getAsString().trim(), nameOption.getAsString().trim())
                .thenAccept(message -> event.getHook().sendMessage(message).queue());
        }
        catch (Exception exception) {
            event.reply("An error occurred while adding the release watch").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to add release watch", exception);
        }
    }
}
