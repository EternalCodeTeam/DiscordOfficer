package com.eternalcode.discordapp.feature.releasewatch;

import com.eternalcode.commons.concurrent.FutureHandler;
import com.eternalcode.discordapp.config.AppConfig;
import com.eternalcode.discordapp.feature.releasewatch.database.ReleaseWatchEntity;
import com.eternalcode.discordapp.feature.releasewatch.database.ReleaseWatchLinkEntity;
import com.eternalcode.discordapp.feature.releasewatch.database.ReleaseWatchRepository;
import io.sentry.Sentry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.std.Result;

public class ReleaseWatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseWatchService.class);
    private static final String INVALID_REPOSITORY_MESSAGE = "Invalid repository, expected format: owner/repo";

    private final AppConfig appConfig;
    private final ReleaseWatchRepository repository;

    public ReleaseWatchService(AppConfig appConfig, ReleaseWatchRepository repository) {
        this.appConfig = appConfig;
        this.repository = repository;
    }

    public CompletableFuture<String> addWatch(String repoSlug, String displayName) {
        Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(repoSlug);
        if (parsed.isErr()) {
            return CompletableFuture.completedFuture(INVALID_REPOSITORY_MESSAGE);
        }

        GitHubRepositoryRef repository = parsed.get();
        String repoId = repository.toSlug().toLowerCase();

        return this.repository.findWatch(repoId).thenCompose(existing -> {
            if (existing.isPresent()) {
                return CompletableFuture.completedFuture("This repository is already being watched");
            }

            if (!ReleaseWatchGitHubClient.repositoryExists(repository, this.appConfig.githubToken)) {
                return CompletableFuture.completedFuture("Repository not found on GitHub: " + repository.toSlug());
            }

            String baselineTag = ReleaseWatchGitHubClient.getLatestRelease(repository, this.appConfig.githubToken)
                .map(GitHubRelease::tagName)
                .orElse(null);

            return this.repository.addWatch(repoId, displayName, baselineTag)
                .thenApply(unused -> "Now watching " + repository.toSlug() + " as \"" + displayName + "\"");
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.error("Failed to add release watch for {}", repoSlug, throwable);
            return "Something went wrong while adding the watch";
        });
    }

    public CompletableFuture<String> removeWatch(String repoSlug) {
        Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(repoSlug);
        if (parsed.isErr()) {
            return CompletableFuture.completedFuture(INVALID_REPOSITORY_MESSAGE);
        }

        String repoId = parsed.get().toSlug().toLowerCase();

        return this.repository.removeWatch(repoId)
            .thenApply(removed -> removed ? "Stopped watching " + repoId : "This repository is not being watched")
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.error("Failed to remove release watch for {}", repoSlug, throwable);
                return "Something went wrong while removing the watch";
            });
    }

    public CompletableFuture<List<ReleaseWatchEntity>> listWatches() {
        return this.repository.listWatches();
    }

    public CompletableFuture<List<ReleaseWatchLinkEntity>> listLinks(String repoId) {
        return this.repository.listLinks(repoId);
    }

    public CompletableFuture<String> addLink(String repoSlug, ReleaseLinkPlatform platform, String value, String customLabel) {
        Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(repoSlug);
        if (parsed.isErr()) {
            return CompletableFuture.completedFuture(INVALID_REPOSITORY_MESSAGE);
        }

        if (platform == ReleaseLinkPlatform.CUSTOM && (customLabel == null || customLabel.isBlank())) {
            return CompletableFuture.completedFuture("A label is required for a custom link");
        }

        if (value == null || value.isBlank()) {
            return CompletableFuture.completedFuture(platform == ReleaseLinkPlatform.MODRINTH
                ? "A Modrinth project slug (or URL) is required"
                : "A URL is required");
        }

        String repoId = parsed.get().toSlug().toLowerCase();
        String label = platform == ReleaseLinkPlatform.CUSTOM ? customLabel.trim() : platform.getDisplayName();
        String storedValue = platform == ReleaseLinkPlatform.MODRINTH
            ? ModrinthClient.extractProjectSlug(value.trim())
            : value.trim();

        return this.repository.findWatch(repoId).thenCompose(existing -> {
            if (existing.isEmpty()) {
                return CompletableFuture.completedFuture("This repository is not being watched yet");
            }

            return this.repository.upsertLink(repoId, platform, label, storedValue)
                .thenApply(unused -> "Added " + label + " link to " + repoId);
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.error("Failed to add release watch link for {}", repoSlug, throwable);
            return "Something went wrong while adding the link";
        });
    }

    public CompletableFuture<String> removeLink(String repoSlug, ReleaseLinkPlatform platform, String customLabel) {
        Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(repoSlug);
        if (parsed.isErr()) {
            return CompletableFuture.completedFuture(INVALID_REPOSITORY_MESSAGE);
        }

        String repoId = parsed.get().toSlug().toLowerCase();

        return this.repository.removeLink(repoId, platform, customLabel)
            .thenApply(removed -> removed ? "Removed the " + platform.getDisplayName() + " link" : "No such link found")
            .exceptionally(throwable -> {
                Sentry.captureException(throwable);
                LOGGER.error("Failed to remove release watch link for {}", repoSlug, throwable);
                return "Something went wrong while removing the link";
            });
    }

    public CompletableFuture<String> sendRelease(JDA jda, String repoSlug, String tag) {
        Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(repoSlug);
        if (parsed.isErr()) {
            return CompletableFuture.completedFuture(INVALID_REPOSITORY_MESSAGE);
        }

        GitHubRepositoryRef repository = parsed.get();
        String repoId = repository.toSlug().toLowerCase();

        return this.repository.findWatch(repoId).thenCompose(existing -> {
            if (existing.isEmpty()) {
                return CompletableFuture.completedFuture("This repository is not being watched yet. Add it first with /release watch-add");
            }

            Optional<GitHubRelease> release = tag == null || tag.isBlank()
                ? ReleaseWatchGitHubClient.getLatestRelease(repository, this.appConfig.githubToken)
                : ReleaseWatchGitHubClient.getReleaseByTag(repository, this.appConfig.githubToken, tag.trim());

            if (release.isEmpty()) {
                return CompletableFuture.completedFuture("No matching release found on GitHub for " + repository.toSlug());
            }

            return this.postRelease(jda, repository, existing.get().getDisplayName(), release.get())
                .thenApply(posted -> posted
                    ? "Sent " + release.get().tagName() + " for " + repository.toSlug()
                    : "Releases channel is not configured or could not be found");
        }).exceptionally(throwable -> {
            Sentry.captureException(throwable);
            LOGGER.error("Failed to send release for {}", repoSlug, throwable);
            return "Something went wrong while sending the release";
        });
    }

    public CompletableFuture<Void> pollForNewReleases(JDA jda) {
        return this.repository.listWatches().thenCompose(watches -> {
            List<CompletableFuture<Void>> pollFutures = watches.stream()
                .map(watch -> this.pollWatch(jda, watch).exceptionally(throwable -> {
                    Sentry.captureException(throwable);
                    LOGGER.warn("Failed to poll release watch: {}", watch.getRepoId(), throwable);
                    return null;
                }))
                .toList();

            return CompletableFuture.allOf(pollFutures.toArray(new CompletableFuture[0]));
        }).exceptionally(FutureHandler::handleException);
    }

    private CompletableFuture<Void> pollWatch(JDA jda, ReleaseWatchEntity watch) {
        return CompletableFuture.supplyAsync(() -> {
            Result<GitHubRepositoryRef, IllegalArgumentException> parsed = GitHubRepositoryRef.fromSlug(watch.getRepoId());
            if (parsed.isErr()) {
                LOGGER.warn("Stored release watch has an invalid repo id: {}", watch.getRepoId());
                return null;
            }

            GitHubRepositoryRef repository = parsed.get();
            Optional<GitHubRelease> latestRelease =
                ReleaseWatchGitHubClient.getLatestRelease(repository, this.appConfig.githubToken);

            if (latestRelease.isEmpty()) {
                return null;
            }

            GitHubRelease release = latestRelease.get();
            if (release.tagName().equals(watch.getLastReleaseTag())) {
                return null;
            }

            this.postRelease(jda, repository, watch.getDisplayName(), release)
                .thenAccept(posted -> {
                    if (posted) {
                        this.repository.updateLastReleaseTag(watch.getRepoId(), release.tagName())
                            .exceptionally(FutureHandler::handleException);
                    }
                })
                .join();

            return null;
        });
    }

    private CompletableFuture<Boolean> postRelease(
        JDA jda,
        GitHubRepositoryRef repository,
        String displayName,
        GitHubRelease release
    ) {
        TextChannel channel = jda.getTextChannelById(this.appConfig.releaseWatch.releasesChannelId);
        if (channel == null) {
            LOGGER.warn("Releases channel not found: {}", this.appConfig.releaseWatch.releasesChannelId);
            return CompletableFuture.completedFuture(false);
        }

        return this.repository.listLinks(repository.toSlug().toLowerCase())
            .thenCompose(links -> this.resolveLinks(links, release))
            .thenCompose(resolvedLinks -> {
                MessageCreateData message =
                    ReleaseEmbedFactory.create(repository, displayName, release, resolvedLinks);

                CompletableFuture<Boolean> future = new CompletableFuture<>();
                channel.sendMessage(message).queue(
                    success -> future.complete(true),
                    failure -> {
                        Sentry.captureException(failure);
                        LOGGER.error("Failed to send release message for {}", repository, failure);
                        future.complete(false);
                    }
                );
                return future;
            });
    }

    private CompletableFuture<List<ResolvedLink>> resolveLinks(List<ReleaseWatchLinkEntity> links, GitHubRelease release) {
        List<CompletableFuture<ResolvedLink>> resolveFutures = links.stream()
            .map(link -> CompletableFuture.supplyAsync(() -> this.resolveLink(link, release)))
            .toList();

        return CompletableFuture.allOf(resolveFutures.toArray(new CompletableFuture[0]))
            .thenApply(unused -> resolveFutures.stream().map(CompletableFuture::join).toList());
    }

    private ResolvedLink resolveLink(ReleaseWatchLinkEntity link, GitHubRelease release) {
        if (ReleaseLinkPlatform.MODRINTH.name().equals(link.getPlatform())) {
            String versionUrl = ModrinthClient.resolveVersionUrl(link.getValue(), release.tagName());
            return new ResolvedLink(link.getLabel(), versionUrl);
        }

        return new ResolvedLink(link.getLabel(), link.getValue());
    }
}
