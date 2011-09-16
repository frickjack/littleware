/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.*;
import littleware.base.Option;
import littleware.db.DbReader;
import littleware.db.DbWriter;

/**
 * DbAssetManager backed by AWS SimpleDB
 */
public class AwsDbAssetManager implements DbAssetManager {
    private final Provider<DbAssetSaver> saverProvider;
    private final Provider<DbAssetLoader> loaderProvider;
    private final Provider<DbAssetDeleter> deleterProvider;
    private final Provider<DbHomeIdLoader> homeIdProvider;
    private final Provider<DbIdsFromLoader.Builder> idsFromProvider;
    private final Provider<DbIdsToLoader.Builder> idsToProvider;
    private final Provider<DbByNameLoader.Builder> byNameProvider;

    
    @Inject
    public AwsDbAssetManager(
            Provider<DbAssetSaver> saverProvider,
            Provider<DbAssetLoader> loaderProvider,
            Provider<DbAssetDeleter> deleterProvider,
            Provider<DbHomeIdLoader> homeIdProvider,
            Provider<DbIdsFromLoader.Builder> idsFromProvider,
            Provider<DbIdsToLoader.Builder> idsToProvider,
            Provider<DbByNameLoader.Builder> byNameProvider
            ) {
        this.saverProvider = saverProvider;
        this.loaderProvider = loaderProvider;
        this.deleterProvider = deleterProvider;
        this.homeIdProvider = homeIdProvider;
        this.idsFromProvider = idsFromProvider;
        this.idsToProvider = idsToProvider;
        this.byNameProvider = byNameProvider;
    }
    
    @Override
    public DbWriter<Asset> makeDbAssetSaver(LittleTransaction trans) {
        return saverProvider.get();
    }

    final DbWriter<AssetType>  noopTypeChecker = new DbWriter<AssetType>() {
            @Override
            public void saveObject(AssetType t) throws SQLException {
            }
        };
    
    /**
     * Return NOOP handler - AWS does not enforce type constraints
     */
    @Override
    public DbWriter<AssetType> makeTypeChecker(LittleTransaction trans) {
        return noopTypeChecker;
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader(LittleTransaction trans) {
        return loaderProvider.get();
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter(LittleTransaction trans) {
        return deleterProvider.get();
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader(LittleTransaction trans) {
        return homeIdProvider.get();
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(LittleTransaction trans, UUID fromId, Option<AssetType> maybeType, Option<Integer> maybeState) {
        return idsFromProvider.get().fromId( fromId ).type( maybeType ).state( maybeState ).build();
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(LittleTransaction trans, UUID toId, Option<AssetType> assetType) {
        return idsToProvider.get().toId( toId ).type( assetType ).build();
    }

    @Override
    public DbReader<Option<Asset>, String> makeDbAssetsByNameLoader(LittleTransaction trans, String name, AssetType assetType) {
        return byNameProvider.get().name(name).type(assetType).build();
    }

    @Override
    public DbReader<List<IdWithClock>, Long> makeLogLoader(LittleTransaction trans, UUID homeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
