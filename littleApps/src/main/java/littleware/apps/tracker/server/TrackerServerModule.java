/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TrackerAssetType;
import littleware.apps.tracker.client.SimpleQueryService;
import littleware.apps.tracker.client.TaskQueryManagerService;
import littleware.asset.AssetException;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.server.AssetSpecializer;
import littleware.base.BaseException;
import littleware.bootstrap.server.AbstractServerModule;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.bootstrap.server.ServerModule;
import littleware.bootstrap.server.ServerModuleFactory;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceFactory;
import littleware.security.auth.server.ServiceFactory;

/**
 * Bind server-side TaskQueryManager implementation, etc.
 */
public class TrackerServerModule extends AbstractServerModule {

    public static class TaskQueryServiceFactory extends AbstractServiceFactory<TaskQueryManagerService> {
        private final TaskQueryManager queryManager;

        @Inject
        public TaskQueryServiceFactory( AssetSearchManager search, TaskQueryManager queryManager ) {
            super(TaskQueryManager.SERVICE_HANDLE, search);
            this.queryManager = queryManager;
        }

        @Override
        public TaskQueryManagerService createServiceProvider(SessionHelper helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return new SimpleQueryService(
                    new RmiTaskQueryManager(this.checkAccessMakeProxy(helper, false, queryManager, TaskQueryManager.class)));
        }
    }

    //-----------------------

    private static final Map<ServiceType, Class<? extends ServiceFactory>> serviceMap;

    static {
        final ImmutableMap.Builder<ServiceType, Class<? extends ServiceFactory>> builder =
                ImmutableMap.builder();
        serviceMap = builder.put(TaskQueryManager.SERVICE_HANDLE, TaskQueryServiceFactory.class ).build();
    }

    //----------------------
    
    private static final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;

    static {
        final ImmutableMap.Builder<AssetType, Class<? extends AssetSpecializer>> builder =
                ImmutableMap.builder();
        typeMap = builder.put( TrackerAssetType.TASK, SimpleTaskSpecializer.class ).build();
    }

    //------------------------

    public static class Factory implements ServerModuleFactory {

        @Override
        public ServerModule build( ServerProfile profile ) {
            return new TrackerServerModule( profile );
        }

    }

    //----------------------------

    private TrackerServerModule( ServerBootstrap.ServerProfile profile ) {
        super( profile, typeMap, serviceMap, emptyServerListeners );
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(TaskQueryManager.class).to(JpaTaskQueryManager.class);
    }
}
