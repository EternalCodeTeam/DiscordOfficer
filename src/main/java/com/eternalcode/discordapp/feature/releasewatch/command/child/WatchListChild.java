package com.eternalcode.discordapp.feature.releasewatch.command.child;

import com.eternalcode.discordapp.feature.releasewatch.ReleaseWatchService;
import com.eternalcode.discordapp.feature.releasewatch.database.ReleaseWatchEntity;
import com.eternalcode.discordapp.feature.releasewatch.database.ReleaseWatchLinkEntity;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatchListChild extends SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchListChild.class);
    private static final int EMBED_FIELD_LIMIT = 25;
    private final ReleaseWatchService releaseWatchService;

    public WatchListChild(ReleaseWatchService releaseWatchService) {
        this.name = "watch-list";
        this.help = "List all watched GitHub repositories";

        this.userPermissions = new Permission[]{ Permission.MANAGE_SERVER };

        this.releaseWatchService = releaseWatchService;
    }

    @Override
    public void execute(SlashCommandEvent event) {
        try {
            event.deferReply(true).queue();

            this.releaseWatchService.listWatches().thenCompose(watches -> {
                if (watches.isEmpty()) {
                    return CompletableFuture.completedFuture(List.<String[]>of());
                }

                List<ReleaseWatchEntity> displayedWatches = watches.size() > EMBED_FIELD_LIMIT
                    ? watches.subList(0, EMBED_FIELD_LIMIT)
                    : watches;

                List<CompletableFuture<String[]>> fieldFutures = displayedWatches.stream()
                    .map(watch -> this.releaseWatchService.listLinks(watch.getRepoId())
                        .thenApply(links -> toField(watch, links)))
                    .toList();

                return CompletableFuture.allOf(fieldFutures.toArray(new CompletableFuture[0]))
                    .thenApply(unused -> fieldFutures.stream().map(CompletableFuture::join).toList());
            }).thenAccept(fields -> {
                if (fields.isEmpty()) {
                    event.getHook().sendMessage("No repositories are being watched yet.").queue();
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder();
                for (String[] field : fields) {
                    embedBuilder.addField(field[0], field[1], false);
                }

                event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            });
        }
        catch (Exception exception) {
            event.reply("An error occurred while listing release watches").setEphemeral(true).queue();
            Sentry.captureException(exception);
            LOGGER.error("Failed to list release watches", exception);
        }
    }

    private static String[] toField(ReleaseWatchEntity watch, List<ReleaseWatchLinkEntity> links) {
        String lastTag = watch.getLastReleaseTag() == null ? "none yet" : watch.getLastReleaseTag();
        String linkList = links.isEmpty()
            ? "no extra links"
            : links.stream().map(ReleaseWatchLinkEntity::getLabel).collect(Collectors.joining(", "));

        return new String[]{
            watch.getDisplayName() + " (" + watch.getRepoId() + ")",
            "Last sent: " + lastTag + "\nLinks: " + linkList
        };
    }
}
