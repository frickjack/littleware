/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap.server;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.server.ServerBootstrap.ServerBuilder;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.bootstrap.server.ServerModuleFactory;


public class SimpleServerBuilder implements ServerBootstrap.ServerBuilder {
    private static final Logger log = Logger.getLogger( SimpleServerBuilder.class.getName() );
    
    private final List<ServerModuleFactory>  factoryList = new ArrayList<ServerModuleFactory>();
    private ServerProfile profile = ServerProfile.J2EE;

    {
        for (ServerModuleFactory moduleFactory : ServiceLoader.load(ServerModuleFactory.class)) {
            factoryList.add(moduleFactory);
        }
        if ( factoryList.isEmpty() ) {
            throw new AssertionFailedException( "Failed to find base server modules: " + ServerModuleFactory.class  );
        }
    }

    @Override
    public Collection<ServerModuleFactory> getModuleSet() {
        return ImmutableList.copyOf( factoryList );
    }

    @Override
    public ServerBuilder addModuleFactory(ServerModuleFactory factory) {
        factoryList.add( factory );
        return this;
    }

    @Override
    public ServerBuilder removeModuleFactory(ServerModuleFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public ServerBuilder profile(ServerProfile profile) {
        this.profile = profile;
        return this;
    }

    private static class Bootstrap extends AbstractLittleBootstrap<ServerModule> implements ServerBootstrap {
        private final ServerProfile profile;

        public Bootstrap( Collection<ServerModule> moduleSet, ServerBootstrap.ServerProfile profile ) {
            super( moduleSet );
            this.profile = profile;
        }

        @Override
        public ServerBootstrap.ServerProfile getProfile() { return profile; }
        
        @Override
        protected <T> T bootstrap( Class<T> bootClass, Collection<? extends ServerModule> moduleSet ) {
            final ImmutableList.Builder<ServerModule> builder = ImmutableList.builder();
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

            return super.bootstrap( bootClass, builder.build() );
        }
    }

    @Override
    public ServerBootstrap build() {
        final ImmutableList.Builder<ServerModule> builder = ImmutableList.builder();
        for( ServerModuleFactory factory : factoryList ) {
            builder.add( factory.build( profile ) );
        }
        return new Bootstrap( builder.build(), profile );
    }

    public static void main(String[] v_argv) {
        log.log(Level.INFO, "Testing OSGi bootstrap");
        final LittleBootstrap boot = ServerBootstrap.provider.get().profile(ServerProfile.Standalone).build();
        boot.bootstrap();
    }
}
 