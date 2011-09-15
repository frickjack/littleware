/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import littleware.bootstrap.internal.SimpleAppBuilder;
import com.google.inject.Provider;
import java.util.Collection;

/**
 * Bootstrap manager for littleware applications.
 * Note that bootstrap actually boots up a new littleware session,
 * so bootstrap( myclass ) is equivalent to:
 *     newSessionBuilder().bootstrap( myclass )
 * Therefore - it's ok to call bootstrap multiple times to start multiple sessions,
 * but shutdown shuts down the whole application, and should only run once.
 */
public interface AppBootstrap extends LittleBootstrap {

    /**
     * Some standard application profiles.
     * JNLP is actually sort of a special SwingApp ...
     */
    public enum AppProfile {
        SwingApp, CliApp, WebApp, JNLP;
    }

    public AppProfile getProfile();
    
    
    //------------------------------------
    
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


