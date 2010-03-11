/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketServiceType;
import littleware.apps.filebucket.client.BucketManagerService;
import littleware.asset.pickle.AssetHumanPickler;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.security.auth.LittleSession;
import littleware.security.auth.ServiceType;



/**
 * Typical Guice setup for littleware.apps.client setup
 */
public class StandardClientGuice implements Module {
    public StandardClientGuice() {
        final ServiceType forceLoad = BucketServiceType.BUCKET_MANAGER;
    }

    /**
     * Just inject the LittleSession into things that
     * want an asset-model injected into the constructor
     */
    public static class DefaultModelProvider implements Provider<AssetModel> {
        private final LittleSession osession;
        private final AssetModelLibrary olibAsset;

        @Inject
        public DefaultModelProvider( LittleSession session, AssetModelLibrary libAsset ) {
            osession = session;
            olibAsset = libAsset;
        }
        @Override
        public AssetModel get() {
            return olibAsset.syncAsset(osession);
        }

    }

    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetModelLibrary.class ).to( SimpleAssetModelLibrary.class ).in( Scopes.SINGLETON );
        binder.bind( AssetModel.class ).toProvider( DefaultModelProvider.class );
        binder.bind( AssetHumanPickler.class ).toProvider( HumanPicklerProvider.class );
        binder.bind( BucketManager.class ).to( BucketManagerService.class );
    }
}
