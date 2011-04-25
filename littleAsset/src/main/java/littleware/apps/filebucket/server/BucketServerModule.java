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
import com.google.inject.Binder;
import java.util.logging.Logger;
import littleware.apps.filebucket.BucketManager;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;


public class BucketServerModule extends AbstractServerModule {
    private static final Logger log = Logger.getLogger(BucketServerModule.class.getName());


    public static class Factory implements ServerModuleFactory {

        @Override
        public ServerModule build(ServerProfile profile ) {
            return new BucketServerModule( profile );
        }

    }

    
    private BucketServerModule( ServerBootstrap.ServerProfile profile ) {
        super( profile );
        // TODO - register server listener for bucket cleanup on asset-delete
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( BucketManager.class ).to( SimpleBucketManager.class );
        binder.bind( DeleteCBProvider.class ).to( SimpleDeleteCBProvider.class );
    }

}
