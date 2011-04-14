/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap;

import littleware.bootstrap.internal.SimpleAppBuilder;
import com.google.inject.Provider;
import java.util.Collection;
import littleware.bootstrap.LittleBootstrap;

/**
 * Bootstrap manager for applications that use
 * some littleware utilities but do not access the
 * littleware node database as a client or implement
 * a littleware server service.
 */
public interface AppBootstrap extends LittleBootstrap {

    /**
     * Some standard application profiles.
     * JNDI is actually sort of a special SwingApp ...
     */
    public enum AppProfile {
        SwingApp, CliApp, WebApp, JNDI;
    }

    @Override
    public Collection<? extends AppModule> getModuleSet();
    public AppProfile getProfile();

    public interface AppBuilder extends LittleBootstrap.Builder {

        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<AppModuleFactory> getModuleSet();

        public AppBuilder addModuleFactory(AppModuleFactory factory);

        public AppBuilder removeModuleFactory(AppModuleFactory factory);

        public AppBuilder profile(AppProfile value);

        @Override
        public AppBootstrap build();
    }

    public static final Provider<AppBuilder> appProvider = new Provider<AppBuilder>() {
        @Override
        public AppBuilder get() {
            return new SimpleAppBuilder();
        }
    };
    
}

