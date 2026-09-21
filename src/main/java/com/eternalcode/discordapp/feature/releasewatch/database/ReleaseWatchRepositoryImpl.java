package com.eternalcode.discordapp.feature.releasewatch.database;

import com.eternalcode.discordapp.database.DataAccessException;
import com.eternalcode.discordapp.database.DatabaseManager;
import com.eternalcode.discordapp.database.repository.AbstractRepository;
import com.eternalcode.discordapp.feature.releasewatch.ReleaseLinkPlatform;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import io.sentry.Sentry;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseWatchRepositoryImpl extends AbstractRepository<ReleaseWatchEntity, String>
    implements ReleaseWatchRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseWatchRepositoryImpl.class);

    private static final String FIELD_ID = "id";
    private static final String FIELD_REPO_ID = "repoId";
    private static final String FIELD_PLATFORM = "platform";
    private static final String FIELD_LABEL = "label";

    private ReleaseWatchRepositoryImpl(DatabaseManager databaseManager) {
        super(databaseManager, ReleaseWatchEntity.class);
    }

    public static ReleaseWatchRepository create(DatabaseManager databaseManager) {
        try {
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), ReleaseWatchEntity.class);
            TableUtils.createTableIfNotExists(databaseManager.getConnectionSource(), ReleaseWatchLinkEntity.class);
            LOGGER.info("Release watch tables initialized successfully");
        }
        catch (SQLException sqlException) {
            Sentry.captureException(sqlException);
            throw new DataAccessException("Failed to create release watch tables", sqlException);
        }

        return new ReleaseWatchRepositoryImpl(databaseManager);
    }

    @Override
    public CompletableFuture<Void> addWatch(String repoId, String displayName, String lastReleaseTag) {
        return this.save(new ReleaseWatchEntity(repoId, displayName, lastReleaseTag)).thenApply(status -> null);
    }

    @Override
    public CompletableFuture<Boolean> removeWatch(String repoId) {
        return this.deleteById(repoId).thenCompose(deletedCount -> {
            if (deletedCount == 0) {
                return CompletableFuture.completedFuture(false);
            }

            return this.action(dao -> {
                Dao<ReleaseWatchLinkEntity, Integer> linkDao = this.databaseManager.getDao(ReleaseWatchLinkEntity.class);
                DeleteBuilder<ReleaseWatchLinkEntity, Integer> deleteBuilder = linkDao.deleteBuilder();
                deleteBuilder.where().eq(FIELD_REPO_ID, repoId);
                deleteBuilder.delete();
                return true;
            });
        });
    }

    @Override
    public CompletableFuture<Optional<ReleaseWatchEntity>> findWatch(String repoId) {
        return this.select(repoId);
    }

    @Override
    public CompletableFuture<List<ReleaseWatchEntity>> listWatches() {
        return this.selectAll();
    }

    @Override
    public CompletableFuture<Void> updateLastReleaseTag(String repoId, String lastReleaseTag) {
        return this.action(dao -> {
            ReleaseWatchEntity entity = dao.queryForId(repoId);
            if (entity == null) {
                LOGGER.warn("Tried to update last release tag for unknown watch: {}", repoId);
                return null;
            }

            entity.setLastReleaseTag(lastReleaseTag);
            dao.update(entity);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> upsertLink(String repoId, ReleaseLinkPlatform platform, String label, String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dao<ReleaseWatchLinkEntity, Integer> linkDao = this.databaseManager.getDao(ReleaseWatchLinkEntity.class);

                ReleaseWatchLinkEntity existing = this.findExistingLink(linkDao, repoId, platform, label);
                if (existing != null) {
                    DeleteBuilder<ReleaseWatchLinkEntity, Integer> deleteBuilder = linkDao.deleteBuilder();
                    deleteBuilder.where().eq(FIELD_ID, existing.getId());
                    deleteBuilder.delete();
                }

                linkDao.create(new ReleaseWatchLinkEntity(repoId, platform.name(), label, value));
                return null;
            }
            catch (SQLException sqlException) {
                Sentry.captureException(sqlException);
                throw new DataAccessException("Failed to upsert release watch link", sqlException);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removeLink(String repoId, ReleaseLinkPlatform platform, String label) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dao<ReleaseWatchLinkEntity, Integer> linkDao = this.databaseManager.getDao(ReleaseWatchLinkEntity.class);
                ReleaseWatchLinkEntity existing = this.findExistingLink(linkDao, repoId, platform, label);

                if (existing == null) {
                    return false;
                }

                DeleteBuilder<ReleaseWatchLinkEntity, Integer> deleteBuilder = linkDao.deleteBuilder();
                deleteBuilder.where().eq(FIELD_ID, existing.getId());
                return deleteBuilder.delete() > 0;
            }
            catch (SQLException sqlException) {
                Sentry.captureException(sqlException);
                throw new DataAccessException("Failed to remove release watch link", sqlException);
            }
        });
    }

    private ReleaseWatchLinkEntity findExistingLink(
        Dao<ReleaseWatchLinkEntity, Integer> linkDao,
        String repoId,
        ReleaseLinkPlatform platform,
        String label
    ) throws SQLException {
        QueryBuilder<ReleaseWatchLinkEntity, Integer> queryBuilder = linkDao.queryBuilder();

        if (platform == ReleaseLinkPlatform.CUSTOM) {
            queryBuilder.where()
                .eq(FIELD_REPO_ID, repoId)
                .and()
                .eq(FIELD_PLATFORM, platform.name())
                .and()
                .eq(FIELD_LABEL, label);
        }
        else {
            queryBuilder.where()
                .eq(FIELD_REPO_ID, repoId)
                .and()
                .eq(FIELD_PLATFORM, platform.name());
        }

        return queryBuilder.queryForFirst();
    }

    @Override
    public CompletableFuture<List<ReleaseWatchLinkEntity>> listLinks(String repoId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dao<ReleaseWatchLinkEntity, Integer> linkDao = this.databaseManager.getDao(ReleaseWatchLinkEntity.class);
                return linkDao.queryBuilder().where().eq(FIELD_REPO_ID, repoId).query();
            }
            catch (SQLException sqlException) {
                Sentry.captureException(sqlException);
                throw new DataAccessException("Failed to list release watch links", sqlException);
            }
        });
    }
}
