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
    public static final String littlewareDomain = "littleware";
    private final Provider<DbAssetSaver> saverProvider;
    private final Provider<DbAssetLoader> loaderProvider;
    private final Provider<DbAssetDeleter> deleterProvider;

    
    @Inject
    public AwsDbAssetManager(
            Provider<DbAssetSaver> saverProvider,
            Provider<DbAssetLoader> loaderProvider,
            Provider<DbAssetDeleter> deleterProvider
            ) {
        this.saverProvider = saverProvider;
        this.loaderProvider = loaderProvider;
        this.deleterProvider = deleterProvider;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(LittleTransaction trans, UUID fromId, Option<AssetType> maybeType, Option<Integer> maybeState) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(LittleTransaction trans, UUID fromId, AssetType childType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(LittleTransaction trans, String name, AssetType assetType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<List<IdWithClock>, Long> makeLogLoader(LittleTransaction trans, UUID homeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
