/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.bootstrap.internal;

import littleware.asset.server.bootstrap.AbstractServerModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.helper.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerBuilder;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleModule;
import littleware.bootstrap.SessionBootstrap.SessionBuilder;
import littleware.bootstrap.helper.SimpleSessionBuilder;



public class SimpleServerBuilder implements ServerBootstrap.ServerBuilder {
    private static final Logger log = Logger.getLogger( SimpleServerBuilder.class.getName() );
    
    private final List<ServerModuleFactory>  serverFactoryList = new ArrayList<>();
    private final List<AppModuleFactory>  appFactoryList = new ArrayList<>();

    private AppProfile profile = AppProfile.CliApp;

    {
        for (ServerModuleFactory moduleFactory : ServiceLoader.load(ServerModuleFactory.class)) {
            serverFactoryList.add(moduleFactory);
        }
        if ( serverFactoryList.isEmpty() ) {
            throw new AssertionFailedException( "Failed to find base server modules: " + ServerModuleFactory.class  );
        }
        for (AppModuleFactory moduleFactory : ServiceLoader.load(AppModuleFactory.class)) {
            appFactoryList.add(moduleFactory);
        }
        if ( appFactoryList.isEmpty() ) {
            throw new AssertionFailedException( "Failed to find base app modules: " + AppModuleFactory.class  );
        }
    }


    @Override
    public ServerBuilder addModuleFactory(ServerModuleFactory factory) {
        serverFactoryList.add( factory );
        return this;
    }

    @Override
    public ServerBuilder removeModuleFactory(ServerModuleFactory factory) {
        serverFactoryList.remove(factory);
        return this;
    }

    
    @Override
    public ServerBuilder profile(AppBootstrap.AppProfile profile) {
        this.profile = profile;
        return this;
    }


    @Override
    public Collection<AppModuleFactory> getModuleSet() {
        return ImmutableList.copyOf( appFactoryList );
    }

    @Override
    public ServerBuilder addModuleFactory(AppModuleFactory factory) {
        appFactoryList.add( factory );
        return this;
    }

    @Override
    public ServerBuilder removeModuleFactory(AppModuleFactory factory) {
        appFactoryList.remove(factory);
        return this;
    }

    @Override
    public Collection<ServerModuleFactory> getServerModuleSet() {
        return serverFactoryList;
    }

    public static class Bootstrap extends AbstractLittleBootstrap<LittleModule> implements ServerBootstrap {
        private final AppBootstrap.AppProfile profile;

        public Bootstrap( Collection<LittleModule> moduleSet, AppBootstrap.AppProfile profile ) {
            super( moduleSet );
            this.profile = profile;
        }

        @Override
        public AppBootstrap.AppProfile getProfile() { return profile; }
        

        public Injector appInjector = null;
        
        /**
         * Application-level bootstrap - initializes app-level injector on first call,
         * then just returns that cached value
         * 
         * @return app-level injector (to create sessions with)
         */
        protected Injector bootstrapApp() {
            if ( null != appInjector ) {
                return appInjector;
            }
            final ImmutableList.Builder<LittleModule> builder = ImmutableList.builder();
            builder.addAll( getModuleSet() );
            builder.add(
                new AbstractServerModule( profile ) {
                    @Override
                    public void configure( Binder binder ) {
                        binder.bind( AppBootstrap.AppProfile.class ).toInstance( profile );
                        binder.bind( LittleBootstrap.class ).to( AppBootstrap.class );
                        binder.bind( AppBootstrap.class ).to( ServerBootstrap.class );
                        binder.bind( ServerBootstrap.class ).toInstance( Bootstrap.this );
                    }
                }
                );
            
            appInjector = super.bootstrapCore( builder.build() );            
            return appInjector;
        }

        
        @Override
        public SessionBuilder newSessionBuilder() {
            return new SimpleSessionBuilder( profile, bootstrapApp() );
        }


        @Override
        public <T> T bootstrap(Class<T> bootClass) {
            log.log( Level.FINE, "Attempting to bootstrap: {0}", bootClass.getName());
            final Injector child = newSessionBuilder().build().startSession(Injector.class);
            if ( child == appInjector ) {
                throw new AssertionFailedException( "What? " );
            }
            log.log( Level.FINE, "... injecting {0}", bootClass.getName());
            return child.getInstance( bootClass );
            //return bootstrapApp().getInstance( bootClass );
        }
        
    }

    @Override
    public ServerBootstrap build() {
        final ImmutableList.Builder<LittleModule> builder = ImmutableList.builder();
        for( ServerModuleFactory factory : serverFactoryList ) {
            builder.add( factory.buildServerModule( profile ) );
        }

        for( AppModuleFactory factory : appFactoryList ) {
            builder.add( factory.build( profile ) );
        }
        return new Bootstrap( builder.build(), profile );
    }

    public static void main(String[] v_argv) {
        log.log(Level.INFO, "Testing OSGi bootstrap");
        final LittleBootstrap boot = ServerBootstrap.provider.get().profile(AppProfile.CliApp).build();
        boot.bootstrap();
    }
}
 