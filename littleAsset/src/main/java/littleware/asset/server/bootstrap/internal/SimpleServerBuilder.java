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
import littleware.bootstrap.helper.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerBuilder;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleModule;


public class SimpleServerBuilder implements ServerBootstrap.ServerBuilder {
    private static final Logger log = Logger.getLogger( SimpleServerBuilder.class.getName() );
    
    private final List<ServerModuleFactory>  serverFactoryList = new ArrayList<ServerModuleFactory>();
    private final List<AppModuleFactory>  appFactoryList = new ArrayList<AppModuleFactory>();

    private ServerProfile profile = ServerProfile.Standalone;

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
    public Collection<ServerModuleFactory> getServerModuleSet() {
        return ImmutableList.copyOf( serverFactoryList );
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
    public ServerBuilder profile(ServerProfile profile) {
        this.profile = profile;
        return this;
    }


    @Override
    public Collection<AppModuleFactory> getAppModuleSet() {
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

    private static class Bootstrap extends AbstractLittleBootstrap<LittleModule> implements ServerBootstrap {
        private final ServerProfile profile;

        public Bootstrap( Collection<LittleModule> moduleSet, ServerBootstrap.ServerProfile profile ) {
            super( moduleSet );
            this.profile = profile;
        }

        @Override
        public ServerBootstrap.ServerProfile getProfile() { return profile; }
        
        @Override
        protected <T> T osgiBootstrap( Class<T> bootClass, Collection<? extends LittleModule> moduleSet ) {
            final ImmutableList.Builder<LittleModule> builder = ImmutableList.builder();
            builder.addAll( moduleSet );
            builder.add(
                new AbstractServerModule( profile ) {
                    @Override
                    public void configure( Binder binder ) {
                        binder.bind( LittleBootstrap.class ).to( ServerBootstrap.class );
                        binder.bind( ServerBootstrap.class ).toInstance( Bootstrap.this );
                    }
                }
                );

            return super.osgiBootstrap( bootClass, builder.build() );
        }

        @Override
        public void bootstrap() {
            bootstrap( Injector.class );
        }

        @Override
        public <T> T bootstrap(Class<T> type) {
            return osgiBootstrap( type, getModuleSet() );
        }

    }

    @Override
    public ServerBootstrap build() {
        final ImmutableList.Builder<LittleModule> builder = ImmutableList.builder();
        for( ServerModuleFactory factory : serverFactoryList ) {
            builder.add( factory.build( profile ) );
        }
        final AppBootstrap.AppProfile appProfile = (profile.equals( ServerProfile.J2EE ) ) ?
            AppBootstrap.AppProfile.WebApp : AppBootstrap.AppProfile.JNLP;

        for( AppModuleFactory factory : appFactoryList ) {
            builder.add( factory.build( appProfile ) );
        }
        return new Bootstrap( builder.build(), profile );
    }

    public static void main(String[] v_argv) {
        log.log(Level.INFO, "Testing OSGi bootstrap");
        final LittleBootstrap boot = ServerBootstrap.provider.get().profile(ServerProfile.Standalone).build();
        boot.bootstrap();
    }
}
 