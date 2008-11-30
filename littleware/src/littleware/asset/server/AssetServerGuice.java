/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        binder.bind( TransactionManager.class ).to( SimpleTransactionManager.class ).in( Scopes.SINGLETON );
        binder.bind( CacheManager.class ).to( SimpleCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbCacheManager.class ).to( DerbyDbCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbAssetManager.class ).to( DbAssetPostgresManager.class ).in( Scopes.SINGLETON );
        binder.bind( QuotaUtil.class ).to( SimpleQuotaUtil.class ).in( Scopes.SINGLETON );
        binder.bind( AssetSpecializerRegistry.class ).to( SimpleSpecializerRegistry.class ).in( Scopes.SINGLETON );
    }

}
