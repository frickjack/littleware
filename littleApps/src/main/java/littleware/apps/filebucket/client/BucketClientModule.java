/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket.client;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketServiceType;
import littleware.apps.filebucket.BucketUtil;
import littleware.apps.filebucket.SimpleBucketUtil;
import littleware.bootstrap.client.AbstractClientModule;
import littleware.bootstrap.client.AppBootstrap;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.ClientModule;
import littleware.bootstrap.client.ClientModuleFactory;
import littleware.security.auth.ServiceType;

/**
 * Client-side bucket-service module setup
 */
public class BucketClientModule extends AbstractClientModule {
    private static final Logger log = Logger.getLogger( BucketClientModule.class.getName() );

    private static final Collection<ServiceType>  typeSet = Collections.singleton( (ServiceType) BucketServiceType.BUCKET_MANAGER );

    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile) {
            return new BucketClientModule(profile);
        }
        
    }

    private BucketClientModule( AppBootstrap.AppProfile profile ) {
        super( profile, emptyAssetTypes,
                typeSet,
                emptyServiceListeners
                );
    }

    @Override
    public void configure( Binder binder ) {
        binder.bind(BucketManager.class).to(BucketManagerService.class).in( Scopes.SINGLETON );
        binder.bind( BucketUtil.class ).to( SimpleBucketUtil.class ).in( Scopes.SINGLETON );
    }

}
