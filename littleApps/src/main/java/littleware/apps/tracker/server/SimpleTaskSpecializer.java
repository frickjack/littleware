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
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetRetriever;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.base.BaseException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * TASK-type specializer manages server-side association of task with queue.
 * The BundleActivator mixin registers the specializer singleton with the
 * specializer registry.
 */
@Singleton
public class SimpleTaskSpecializer implements AssetSpecializer, BundleActivator {

    @Inject
    public SimpleTaskSpecializer( AssetSpecializerRegistry registry )
    {
        registry.registerService( TrackerAssetType.TASK, this );
    }


    @Override
    public <T extends Asset> T narrow(T t, AssetRetriever ar) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void postCreateCallback(Asset asset, AssetManager am) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void postUpdateCallback(Asset asset, Asset asset1, AssetManager am) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void postDeleteCallback(Asset asset, AssetManager am) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void start(BundleContext bc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
