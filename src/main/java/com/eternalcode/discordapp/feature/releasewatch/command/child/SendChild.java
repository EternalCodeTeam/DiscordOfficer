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

public class SendChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendChild.class);
    private final ReleaseWatchService releaseWatchService;

    public SendChild(ReleaseWatchService releaseWatchService) {
        this.name = "send";
        this.help = "Manually (re)send a release to the releases channel, e.g. a test or a backfilled one";

        this.userPermissions = new Permission[]{ Permission.MANAGE_SERVER };

        this.options = List.of(
            new OptionData(OptionType.STRING, ReleaseCommandOptions.REPO, "GitHub repository in owner/repo format")
                .setRequired(true),
            new OptionData(OptionType.STRING, ReleaseCommandOptions.TAG, "Release tag to send, defaults to the latest release")
                .setRequired(false)
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

            OptionMapping tagOption = event.getOption(ReleaseCommandOptions.TAG);
            String tag = tagOption == null ? null : tagOption.getAsString().trim();

            event.deferReply(true).queue();

            this.releaseWatchService.sendRelease(event.getJDA(), repoOption.getAsString().trim(), tag)
                .thenAccept(message -> event.getHook().sendMessage(message).queue());
        }
        catch (Exception exception) {
            event.reply("An error occurred while sending the release").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to send release", exception);
        }
    }
}
