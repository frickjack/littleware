/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import java.util.logging.Logger;

import littleware.asset.*;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.server.db.DbCacheManager;
import littleware.asset.server.db.derby.DerbyDbCacheManager;
import littleware.asset.server.db.postgres.DbAssetPostgresManager;


/**
 * Simple server-side guice module for littleware.asset
 */
public class AssetServerGuice implements Module {
    private static final Logger olog = Logger.getLogger( AssetServerGuice.class.getName () );

    public void configure(Binder binder) {
        binder.bind( AssetManager.class ).to( SimpleAssetManager.class ).in( Scopes.SINGLETON );
        binder.bind( AssetRetriever.class ).to( AssetSearchManager.class ).in( Scopes.SINGLETON );
        binder.bind( AssetSearchManager.class ).to( SimpleAssetSearchManager.class ).in( Scopes.SINGLETON );
        binder.bind( LittleTransaction.class ).to( SimpleLittleTransaction.class );
        binder.bind( TransactionManager.class ).to( SimpleTransactionManager.class ).in( Scopes.SINGLETON );
        binder.bind( CacheManager.class ).to( SimpleCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbCacheManager.class ).to( DerbyDbCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbAssetManager.class ).to( DbAssetPostgresManager.class ).in( Scopes.SINGLETON );
        binder.bind( QuotaUtil.class ).to( SimpleQuotaUtil.class ).in( Scopes.SINGLETON );
        binder.bind( AssetSpecializerRegistry.class ).to( SimpleSpecializerRegistry.class ).in( Scopes.SINGLETON );
    }

}
