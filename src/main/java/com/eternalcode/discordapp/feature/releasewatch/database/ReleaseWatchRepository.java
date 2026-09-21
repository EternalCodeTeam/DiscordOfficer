package com.eternalcode.discordapp.feature.releasewatch.database;

import com.eternalcode.discordapp.feature.releasewatch.ReleaseLinkPlatform;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ReleaseWatchRepository {

    CompletableFuture<Void> addWatch(String repoId, String displayName, String lastReleaseTag);

    CompletableFuture<Boolean> removeWatch(String repoId);

    CompletableFuture<Optional<ReleaseWatchEntity>> findWatch(String repoId);

    CompletableFuture<List<ReleaseWatchEntity>> listWatches();

    CompletableFuture<Void> updateLastReleaseTag(String repoId, String lastReleaseTag);

    CompletableFuture<Void> upsertLink(String repoId, ReleaseLinkPlatform platform, String label, String value);

    CompletableFuture<Boolean> removeLink(String repoId, ReleaseLinkPlatform platform, String label);

    CompletableFuture<List<ReleaseWatchLinkEntity>> listLinks(String repoId);
}
