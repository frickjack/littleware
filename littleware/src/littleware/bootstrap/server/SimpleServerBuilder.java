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
import java.util.logging.Logger;
import littleware.bootstrap.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.server.ServerBootstrap.ServerBuilder;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.bootstrap.server.ServerModule.ServerFactory;


public class SimpleServerBuilder implements ServerBootstrap.ServerBuilder {
    private static final Logger log = Logger.getLogger( SimpleServerBuilder.class.getName() );
    
    private final List<ServerFactory>  moduleList = new ArrayList<ServerFactory>();
    private ServerProfile profile = ServerProfile.J2EE;

    @Override
    public Collection<ServerFactory> getModuleList() {
        return ImmutableList.copyOf( moduleList );
    }

    @Override
    public ServerBuilder addModuleFactory(ServerFactory factory) {
        moduleList.add( factory );
        return this;
    }

    @Override
    public ServerBuilder removeModuleFactory(ServerFactory factory) {
        moduleList.remove(factory);
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
            final ImmutableList.Builder<? extends ServerModule> builder = ImmutableList.builder();
            builder.add(
                new AbstractServerModule( profile ) {
                    @Override
                    public void configure( Binder binder ) {
                        binder.bind( LittleBootstrap.class ).to( ServerBootstrap.class );
                        binder.bind( ServerBootstrap.class ).toInstance( Bootstrap.this );
                    }
                }
                );

            return super.bootstrap( bootClass, moduleSet );
        }
    }

    @Override
    public ServerBootstrap build() {
        final ImmutableList.Builder<ServerModule> builder = ImmutableList.builder();
        for( ServerModule.ServerFactory factory : moduleList ) {
            builder.add( factory.build( profile ) );
        }
        return new Bootstrap( builder.build(), profile );
    }

}

/**
 * Singleton class bootstraps littleware server.
 * First loads lw.guice_modules and lw.osgi_bundles properties
 * from littleware.properties.  Next builds a Guice injector
 * with the specified modules.  Finally, launches the OSGi
 * bundle activators within an embedded OSGi environment.
 *
 * Note: server environment differs from client environment.
 * A global shared AssetManager, AssetSearchManager, etc. rely
 * on the underlying RMI runtime to setup a proper JAAS
 * environment from which the user associated with some
 * operation may be derived.  It is up to the server implementation
 * to enforce security constraints.  The injected search and asset
 * managers take care of this for many purposes.
 *
 * On the client each manager
 * is associated with the client session associated with
 * the SessionHelper from which the manager is derived.
 *
public class ServerBootstrap extends AbstractGOBootstrap {

    private static final Logger log = Logger.getLogger(ServerBootstrap.class.getName());

    public ServerBootstrap() {
        this(false);
    }

    @VisibleForTesting
    public ServerBootstrap(boolean bHibernate) {
        super(
                Arrays.asList(
                //new PostgresGuice(),
                bHibernate ? new HibernateGuice() : new J2EEGuice(),
                new AssetServerGuice(),
                new AuthServerGuice(),
                new SecurityServerGuice()),
                Arrays.asList(
                ServerActivator.class,
                SecurityServerActivator.class),
                true);
        try {
            getGuiceModule().add(
                    new DbGuice(
                    PropertiesLoader.get().loadProperties("littleware_jdbc.properties")));
        } catch (IOException ex) {
            log.log(Level.INFO, "Skipping littleware_jdbc.properties injection", ex);
        }
    }

    public static void main(String[] v_argv) {
        log.log(Level.INFO, "Testing OSGi bootstrap");
        LittleBootstrap boot = new ServerBootstrap();
        boot.bootstrap();
        log.log(Level.INFO, "Sleeping 10 seconds before shutdown");
        try {
            Thread.sleep(10000);
            boot.shutdown();
            log.log(Level.INFO, "Shutdown issued, sleep 5 seconds before exit");
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
 */