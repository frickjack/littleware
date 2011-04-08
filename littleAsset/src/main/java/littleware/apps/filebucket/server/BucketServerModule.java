/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.filebucket.server;

import littleware.apps.filebucket.server.internal.SimpleDeleteCBProvider;
import littleware.apps.filebucket.server.internal.SimpleBucketManager;
import littleware.apps.filebucket.server.internal.RmiBucketManager;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketServiceType;
import littleware.apps.filebucket.client.BucketManagerService;
import littleware.apps.filebucket.client.internal.SimpleBucketService;
import littleware.asset.AssetException;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceFactory;
import littleware.security.auth.server.ServiceFactory;

public class BucketServerModule extends AbstractServerModule {
    private static final Logger log = Logger.getLogger(BucketServerModule.class.getName());


    public static class Factory implements ServerModuleFactory {

        @Override
        public ServerModule build(ServerProfile profile ) {
            return new BucketServerModule( profile );
        }

    }

    public static class BucketServiceFactory extends AbstractServiceFactory<BucketManagerService> {

        private final BucketManager bucketManager;

        @Inject
        public BucketServiceFactory(AssetSearchManager search, BucketManager bucketManager) {
            super(BucketServiceType.BUCKET_MANAGER, search);
            this.bucketManager = bucketManager;
        }

        @Override
        public BucketManagerService createServiceProvider(SessionHelper helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return new SimpleBucketService(new RmiBucketManager(this.checkAccessMakeProxy(helper, false, bucketManager, BucketManager.class)));
        }
    }

    private static final Map<ServiceType, Class<? extends ServiceFactory>> serviceMap;
    static {
        final ImmutableMap.Builder<ServiceType, Class<? extends ServiceFactory>> builder =
                ImmutableMap.builder();
        serviceMap = builder.put( BucketServiceType.BUCKET_MANAGER, BucketServiceFactory.class ).build();
    }

    private BucketServerModule( ServerBootstrap.ServerProfile profile ) {
        super( profile, emptyTypeMap, serviceMap, emptyServerListeners );
        // TODO - register server listener for bucket cleanup on asset-delete
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( BucketManager.class ).to( SimpleBucketManager.class );
        binder.bind( DeleteCBProvider.class ).to( SimpleDeleteCBProvider.class );
    }

}
