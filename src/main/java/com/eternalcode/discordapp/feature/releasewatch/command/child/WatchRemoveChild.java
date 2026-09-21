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

public class WatchRemoveChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchRemoveChild.class);
    private final ReleaseWatchService releaseWatchService;

    public WatchRemoveChild(ReleaseWatchService releaseWatchService) {
        this.name = "watch-remove";
        this.help = "Stop watching a GitHub repository for new releases";

        this.userPermissions = new Permission[]{ Permission.MANAGE_SERVER };

        this.options = List.of(
            new OptionData(OptionType.STRING, ReleaseCommandOptions.REPO, "GitHub repository in owner/repo format")
                .setRequired(true)
        );

        this.releaseWatchService = releaseWatchService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            OptionMapping repoOption = event.getOption(ReleaseCommandOptions.REPO);
            if (repoOption == null) {
                event.reply("Missing required option: repo").setEphemeral(true).queue();
                return;
            }

            event.deferReply(true).queue();

            this.releaseWatchService.removeWatch(repoOption.getAsString().trim())
                .thenAccept(message -> event.getHook().sendMessage(message).queue());
        }
        catch (Exception exception) {
            event.reply("An error occurred while removing the release watch").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to remove release watch", exception);
        }
    }
}
