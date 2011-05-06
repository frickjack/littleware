/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.Option;
import littleware.db.DbReader;
import littleware.db.DbWriter;

/**
 * JPA implementation of DbAssetManager
 */
public class JpaDbAssetManager implements DbAssetManager {

    private static final Logger log = Logger.getLogger(JpaDbAssetManager.class.getName());
    private final AssetProviderRegistry assetRegistry;
    private final Provider<IdWithClock.Builder> clockBuilder;

    @Inject
    public JpaDbAssetManager(
            AssetProviderRegistry assetRegistry,
            Provider<IdWithClock.Builder> clockBuilder
            )
    {
        this.assetRegistry = assetRegistry;
        this.clockBuilder = clockBuilder;
    }

    @Override
    public int getClientId() {
        return 0;
    }

    @Override
    public void setClientId(int i_id) {
        log.log(Level.INFO, "JpaDbAssetManager ignores clientId property");
    }

    @Override
    public DbWriter<Asset> makeDbAssetSaver( LittleTransaction trans ) {
        return new DbAssetSaver( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader( LittleTransaction trans ) {
        return new DbAssetLoader( (JpaLittleTransaction) trans, assetRegistry );
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter( LittleTransaction trans ) {
        return new DbAssetDeleter( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader( LittleTransaction trans ) {
        return new DbHomeLoader( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader( LittleTransaction trans, UUID fromId, Option<AssetType> maybeType, Option<Integer> maybeState) {
        return new DbIdsFromLoader( (JpaLittleTransaction) trans, fromId, maybeType, maybeState );
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(LittleTransaction trans, UUID toId, AssetType atype) {
        return new DbIdsToLoader( (JpaLittleTransaction) trans, toId, atype);
    }

    @Override
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(LittleTransaction trans, String name, AssetType aType) {
        return new DbByNameLoader( (JpaLittleTransaction) trans, assetRegistry, name, aType);
    }


    @Override
    public DbReader<List<IdWithClock>, Long> makeLogLoader( LittleTransaction trans, UUID homeId ) {
        return new DbLogLoader( (JpaLittleTransaction) trans, clockBuilder.get(), homeId );
    }

    @Override
    public DbWriter<AssetType> makeTypeChecker( LittleTransaction trans ) {
        return new DbTypeChecker( (JpaLittleTransaction) trans );
    }
}
