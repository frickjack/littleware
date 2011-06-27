/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.bootstrap;

import littleware.asset.server.bootstrap.internal.SimpleServerBuilder;
import com.google.inject.Provider;
import java.util.Collection;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.LittleModule;

/**
 * Server-side bootstrap configuration manager
 */
public interface ServerBootstrap extends LittleBootstrap {

    public enum ServerProfile {
        Standalone, J2EE;
    }

    public ServerBootstrap.ServerProfile getProfile();

    @Override
    public Collection<? extends LittleModule> getModuleSet();

    public interface ServerBuilder extends LittleBootstrap.Builder {

        /**
         * List of server modules registered
         */
        public Collection<ServerModuleFactory> getServerModuleSet();
        public ServerBuilder addModuleFactory(ServerModuleFactory factory);
        public ServerBuilder removeModuleFactory(ServerModuleFactory factory);

        /**
         * List of app modules registered
         */
        public Collection<AppModuleFactory> getAppModuleSet();
        public ServerBuilder addModuleFactory(AppModuleFactory factory);
        public ServerBuilder removeModuleFactory(AppModuleFactory factory);


        public ServerBuilder profile(ServerProfile profile);

        @Override
        public ServerBootstrap build();
    }

    
    public static Provider<ServerBuilder> provider = new Provider<ServerBuilder>() {

        @Override
        public ServerBuilder get() {
            return new SimpleServerBuilder();
        }
    };

}
