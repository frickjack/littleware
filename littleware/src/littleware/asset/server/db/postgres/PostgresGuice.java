/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.postgres;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import javax.sql.DataSource;
import littleware.asset.server.CacheManager;
import littleware.asset.server.JdbcTransaction;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.NullCacheManager;
import littleware.asset.server.SimpleLittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.base.ThreadLocalProvider;

/**
 *
 * @author pasquini
 */
public class PostgresGuice implements Module {

    private static class TransactionProvider extends ThreadLocalProvider<SimpleLittleTransaction> {
        private final DataSource odatasource;

        @Inject
        public TransactionProvider( @Named("datasource.littleware") DataSource datasource ) {
            odatasource = datasource;
        }
        @Override
        protected SimpleLittleTransaction build() {
            return new SimpleLittleTransaction( odatasource );
        }
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( LittleTransaction.class ).to( JdbcTransaction.class );
        binder.bind( JdbcTransaction.class ).to( SimpleLittleTransaction.class );
        binder.bind( SimpleLittleTransaction.class ).toProvider( TransactionProvider.class );
        binder.bind( TransactionProvider.class ).in( Scopes.SINGLETON );
        binder.bind( CacheManager.class ).to( NullCacheManager.class ).in( Scopes.SINGLETON );
        //binder.bind( DbCacheManager.class ).to( DerbyDbCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbAssetManager.class ).to( DbAssetPostgresManager.class ).in( Scopes.SINGLETON );
    }

}
