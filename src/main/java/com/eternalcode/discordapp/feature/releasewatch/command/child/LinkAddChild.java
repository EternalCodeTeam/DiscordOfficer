package com.eternalcode.discordapp.feature.releasewatch.command.child;

import com.eternalcode.discordapp.feature.releasewatch.ReleaseLinkPlatform;
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

public class LinkAddChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkAddChild.class);
    private final ReleaseWatchService releaseWatchService;

    public LinkAddChild(ReleaseWatchService releaseWatchService) {
        this.name = "link-add";
        this.help = "Attach a platform link (Modrinth, Hangar, ...) to a watched repository";

        this.userPermissions = new Permission[]{ Permission.MANAGE_SERVER };

        this.options = List.of(
            new OptionData(OptionType.STRING, ReleaseCommandOptions.REPO, "GitHub repository in owner/repo format")
                .setRequired(true),
            new OptionData(OptionType.STRING, ReleaseCommandOptions.PLATFORM, "Link platform")
                .setRequired(true)
                .addChoice(ReleaseLinkPlatform.MODRINTH.getDisplayName(), ReleaseLinkPlatform.MODRINTH.name())
                .addChoice(ReleaseLinkPlatform.HANGAR.getDisplayName(), ReleaseLinkPlatform.HANGAR.name())
                .addChoice(ReleaseLinkPlatform.SPIGOTMC.getDisplayName(), ReleaseLinkPlatform.SPIGOTMC.name())
                .addChoice(ReleaseLinkPlatform.CUSTOM.getDisplayName(), ReleaseLinkPlatform.CUSTOM.name()),
            new OptionData(
                OptionType.STRING,
                ReleaseCommandOptions.VALUE,
                "Modrinth project slug/URL, or the full URL for other platforms"
            ).setRequired(true),
            new OptionData(OptionType.STRING, ReleaseCommandOptions.LABEL, "Display label, required for Custom links")
                .setRequired(false)
        );

        this.releaseWatchService = releaseWatchService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            OptionMapping repoOption = event.getOption(ReleaseCommandOptions.REPO);
            OptionMapping platformOption = event.getOption(ReleaseCommandOptions.PLATFORM);
            OptionMapping valueOption = event.getOption(ReleaseCommandOptions.VALUE);
            if (repoOption == null || platformOption == null || valueOption == null) {
                event.reply("Missing required options: repo, platform and value").setEphemeral(true).queue();
                return;
            }

            ReleaseLinkPlatform platform;
            try {
                platform = ReleaseLinkPlatform.valueOf(platformOption.getAsString());
            }
            catch (IllegalArgumentException exception) {
                event.reply("Unknown platform").setEphemeral(true).queue();
                return;
            }

            OptionMapping labelOption = event.getOption(ReleaseCommandOptions.LABEL);
            String label = labelOption == null ? null : labelOption.getAsString().trim();

            event.deferReply(true).queue();

            this.releaseWatchService.addLink(repoOption.getAsString().trim(), platform, valueOption.getAsString().trim(), label)
                .thenAccept(message -> event.getHook().sendMessage(message).queue());
        }
        catch (Exception exception) {
            event.reply("An error occurred while adding the link").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to add release watch link", exception);
        }
    }
}
