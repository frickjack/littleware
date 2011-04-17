/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.internal.SimpleTaskManager;
import littleware.apps.tracker.TaskManager;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TrackerAssetType;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.asset.AssetType;
import littleware.asset.client.bootstrap.AbstractClientModule;
import littleware.asset.client.bootstrap.ClientModule;
import littleware.asset.client.bootstrap.ClientModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.security.auth.ServiceType;

/**
 * Force load of TaskQueryManager so SERVICE_HANDLE gets registered,
 * bind TaskQueryManager to TaskQueryManagerService
 */
public class TrackerClientModule extends AbstractClientModule {

    private static final Logger log = Logger.getLogger(TrackerClientModule.class.getName());
    private static final Collection<AssetType> typeSet;

    static {
        final ImmutableList.Builder<AssetType> builder = ImmutableList.builder();
        typeSet = builder.add(TrackerAssetType.COMMENT).add(TrackerAssetType.DEPENDENCY).add(TrackerAssetType.QUEUE).add(TrackerAssetType.TASK).add(Product.ProductType).add(ProductAlias.PAType).add(Version.VersionType).add(VersionAlias.VAType).add(Member.MemberType).add(MemberAlias.MAType).build();
    }
    private static final Collection<ServiceType> serviceSet =
            Collections.singleton((ServiceType) TaskQueryManager.SERVICE_HANDLE);

    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile) {
            return new TrackerClientModule(profile);
        }
    }


    private TrackerClientModule(AppBootstrap.AppProfile profile) {
        super(profile, typeSet, serviceSet, emptyServiceListeners);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(TaskQueryManager.class).to(TaskQueryManagerService.class);
        binder.bind(TaskManager.class).to(SimpleTaskManager.class).in(Scopes.SINGLETON);
        binder.bind(ProductManager.class).to(SimpleProductManager.class).in(Scopes.SINGLETON);
        (new TrackerAssetType()).configure( binder );
    }
}
