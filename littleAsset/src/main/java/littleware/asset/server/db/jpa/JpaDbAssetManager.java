/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
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
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.db.DbAssetManager;
import littleware.base.Maybe;
import littleware.db.DbReader;
import littleware.db.DbWriter;

/**
 * JPA implementation of DbAssetManager
 */
public class JpaDbAssetManager implements DbAssetManager {

    private static final Logger olog = Logger.getLogger(JpaDbAssetManager.class.getName());
    private final Provider<DbAssetLoader> oprovideLoader;
    private final Provider<DbAssetSaver> oprovideSaver;
    private final Provider<DbAssetDeleter> oprovideDeleter;
    private final Provider<JpaLittleTransaction> oprovideTrans;
    private final Provider<DbHomeLoader> oprovideHomeLoader;
    private final DbLogLoader.Builder dbLogBuilder;
    private final Provider<DbTypeChecker> oprovideTypeChecker;
    private final AssetSpecializerRegistry assetRegistry;

    @Inject
    public JpaDbAssetManager(
            Provider<JpaLittleTransaction> provideTrans,
            Provider<DbAssetLoader> provideLoader,
            Provider<DbAssetSaver> provideSaver,
            Provider<DbAssetDeleter> provideDeleter,
            Provider<DbHomeLoader> provideHomeLoader,
            Provider<DbTypeChecker> provideTypeChecker,
            DbLogLoader.Builder dbLogBuilder,
            AssetSpecializerRegistry assetRegistry
            )
    {
        oprovideLoader = provideLoader;
        oprovideSaver = provideSaver;
        oprovideDeleter = provideDeleter;
        oprovideTrans = provideTrans;
        oprovideHomeLoader = provideHomeLoader;
        oprovideTypeChecker = provideTypeChecker;
        this.dbLogBuilder = dbLogBuilder;
        this.assetRegistry = assetRegistry;
    }

    @Override
    public int getClientId() {
        return 0;
    }

    @Override
    public void setClientId(int i_id) {
        olog.log(Level.INFO, "JpaDbAssetManager ignores clientId property");
    }

    @Override
    public DbWriter<Asset> makeDbAssetSaver() {
        return oprovideSaver.get();
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader() {
        return oprovideLoader.get();
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter() {
        return oprovideDeleter.get();
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader() {
        return oprovideHomeLoader.get();
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID uFrom, Maybe<AssetType> maybeType, Maybe<Integer> maybeState) {
        return new DbIdsFromLoader(oprovideTrans, uFrom, maybeType, maybeState );
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID uTo, AssetType atype) {
        return new DbIdsToLoader(oprovideTrans, uTo, atype);
    }

    @Override
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(String sName, AssetType aType) {
        return new DbByNameLoader(oprovideTrans, assetRegistry, sName, aType);
    }

    @Override
    public DbWriter<String> makeDbCacheSyncClearer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Map<UUID, Asset>, String> makeDbCacheSyncLoader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public DbReader<List<IdWithClock>, Long> makeLogLoader( UUID homeId ) {
        return dbLogBuilder.build(homeId);
    }

    @Override
    public DbWriter<AssetType> makeTypeChecker() {
        return oprovideTypeChecker.get();
    }
}
