/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

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

    @Override
    public DbWriter<Asset> makeDbAssetSaver(LittleTransaction trans) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter(LittleTransaction trans) {
        throw new UnsupportedOperationException("Not supported yet.");
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
