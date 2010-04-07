/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.server;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TrackerAssetType;
import littleware.apps.tracker.client.SimpleQueryService;
import littleware.apps.tracker.client.TaskQueryManagerService;
import littleware.asset.AssetException;
import littleware.asset.AssetSearchManager;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.base.BaseException;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceProviderFactory;
import littleware.security.auth.server.ServiceProviderRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author pasquini
 */
public class TrackerServerActivator implements BundleActivator {

    @Inject
    public TrackerServerActivator(
            ServiceProviderRegistry serviceRegistry,
            AssetSpecializerRegistry specializerRegistry,
            SimpleTaskSpecializer     taskSpecializer,
            final TaskQueryManager         taskQueryManager,
            AssetSearchManager        search
            )
    {
        specializerRegistry.registerService(TrackerAssetType.TASK, taskSpecializer);
        serviceRegistry.registerService( TaskQueryManager.SERVICE_HANDLE,
                new AbstractServiceProviderFactory<TaskQueryManagerService> ( TaskQueryManager.SERVICE_HANDLE, search ) {
                    @Override
                    public TaskQueryManagerService createServiceProvider(SessionHelper helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new SimpleQueryService(
                                    new RmiTaskQueryManager( this.checkAccessMakeProxy(helper, false, taskQueryManager, TaskQueryManager.class ))
                                    );
                    }
                }
        );

    }

    @Override
    public void start(BundleContext bc) throws Exception {
        // NOOP
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        // NOOP
    }

}
