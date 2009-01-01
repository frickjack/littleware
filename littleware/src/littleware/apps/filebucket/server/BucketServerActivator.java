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

package littleware.apps.filebucket.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketServiceType;
import littleware.asset.AssetException;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.server.AbstractServiceProviderFactory;
import littleware.security.auth.server.ServiceProviderRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BucketServerActivator implements BundleActivator {
    private static final Logger  olog = Logger.getLogger( BucketServerActivator.class.getName() );


    @Inject
    public BucketServerActivator( final BucketManager mgr_bucket,
            final AssetSearchManager mgr_search,
            ServiceProviderRegistry reg_service
            )
    {
        reg_service.registerService( BucketServiceType.BUCKET_MANAGER,
                new AbstractServiceProviderFactory<BucketManager> ( BucketServiceType.BUCKET_MANAGER, mgr_search ) {
                    @Override
                    public BucketManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new RmiBucketManager( this.checkAccessMakeProxy(m_helper, false, mgr_bucket, BucketManager.class ));
                    }
                }
        );
    }

    public void start(BundleContext arg0) throws Exception {
    }

    public void stop(BundleContext arg0) throws Exception {
    }
}
