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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import littleware.asset.server.CacheManager;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.NullCacheManager;
import littleware.asset.server.db.DbAssetManager;
import littleware.base.ThreadLocalProvider;

/**
 * Base Guice module for JPA database backend -
 * sets up JPA friendly TransactionManager,
 * NullCacheManager, etc.
 */
public abstract class AbstractGuice implements Module {
    private static final Logger olog = Logger.getLogger( AbstractGuice.class.getName() );
    /** Guice provider calls through to injected EntityManagerFactory */
    public static class EntityManagerProvider implements Provider<EntityManager> {
        private final EntityManagerFactory ofactory;
        @Inject
        public EntityManagerProvider( EntityManagerFactory factory ) {
            ofactory = factory;
        }
        @Override
        public EntityManager get() {
            return ofactory.createEntityManager();
        }

    }

    public static class TransactionProvider extends ThreadLocalProvider<SimpleJpaTransaction> {
        private Provider<EntityManager> oprovideEntMgr;
        @Inject
        public TransactionProvider( Provider<EntityManager> provideEntMgr ) {
            oprovideEntMgr = provideEntMgr;
        }

        @Override
        protected SimpleJpaTransaction build() {
            return new SimpleJpaTransaction( oprovideEntMgr );
        }
    }


    @Override
    public void configure( Binder binder ) {
        olog.log( Level.FINE, "Configuring JPA database access" );
        binder.bind( CacheManager.class ).to( NullCacheManager.class ).in( Scopes.SINGLETON );
        binder.bind( LittleTransaction.class ).to( JpaLittleTransaction.class );
        binder.bind( JpaLittleTransaction.class ).to( SimpleJpaTransaction.class );
        binder.bind( SimpleJpaTransaction.class ).toProvider(TransactionProvider.class);
        binder.bind( TransactionProvider.class ).in( Scopes.SINGLETON );
        binder.bind( EntityManager.class ).toProvider( EntityManagerProvider.class );
        binder.bind( DbAssetManager.class ).to( JpaDbAssetManager.class ).in( Scopes.SINGLETON );
    }
}
