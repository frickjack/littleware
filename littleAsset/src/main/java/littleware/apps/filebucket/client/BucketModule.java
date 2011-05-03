/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket.client;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketUtil;
import littleware.apps.filebucket.SimpleBucketUtil;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;

/**
 * Client-side bucket-service module setup
 */
public class BucketModule extends AbstractAppModule {
    private static final Logger log = Logger.getLogger( BucketModule.class.getName() );

    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new BucketModule(profile);
        }
        
    }

    private BucketModule( AppBootstrap.AppProfile profile ) {
        super( profile);
    }

    @Override
    public void configure( Binder binder ) {
        binder.bind(BucketManager.class).to(BucketManagerService.class).in( Scopes.SINGLETON );
        binder.bind( BucketUtil.class ).to( SimpleBucketUtil.class ).in( Scopes.SINGLETON );
    }

}
