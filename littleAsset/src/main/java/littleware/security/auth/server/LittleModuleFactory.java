/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.bootstrap.AppBootstrap.AppProfile;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Littleware server-bootstrap module takes care of injecting dependencies
 * into the LittleLoginModule.  This call is registered with littleware bootstrap
 * via META-INF/services/...ServerModuleFactory
 */
public class LittleModuleFactory implements ServerModuleFactory {

    public static class StartupModule extends AbstractServerModule {
        public StartupModule( AppProfile profile ) {
            super( profile );
        }

        @Override
        public void configure(Binder binder) {
            binder.bind( ServerConfigFactory.class ).in( Scopes.SINGLETON );
        }
        
        @Override
        public Class<? extends BundleActivator> getActivator() {
            return Activator.class;
        }
        
        /**
         * Activator takes care of registering LittleLoginModule.injectTools
         */
        public static class Activator implements BundleActivator {
            @Inject
            public Activator( LittleLoginModule.Tools loginTools ) {
                LittleLoginModule.injectTools(loginTools);
            }

            @Override
            public void start(BundleContext bc) throws Exception {
            }

            @Override
            public void stop(BundleContext bc) throws Exception {
            }
        }
    }
    
    @Override
    public ServerModule buildServerModule(AppProfile profile) {
        return new StartupModule( profile );
    }
    
}
