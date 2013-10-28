/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.security.auth.server.internal.SimpleSessionManager;


/**
 * Bootstrap configuration for authentication component of
 * littleware server.
 */
public class LittleModuleFactory implements ServerModuleFactory {

    public static class StartupModule extends AbstractServerModule {
        public StartupModule( AppProfile profile ) {
            super( profile );
        }

        @Override
        public void configure(Binder binder) {
            binder.bind( ServerConfigFactory.class ).in( Scopes.SINGLETON );
            //
            // TODO - do something smarter than this to support
            //   bootstrap specification of an administrator user
            //   ready to authenticate with whatever JAAS config
            //   the server is being deployed under
            //
            binder.bind( SimpleSessionManager.RuntimeConfig.class ).toInstance(
                    new SimpleSessionManager.RuntimeConfig( "reuben@frickjack.com" )
                    );
        }     
    }
    
    @Override
    public ServerModule buildServerModule(AppProfile profile) {
        return new StartupModule( profile );
    }
    
}
