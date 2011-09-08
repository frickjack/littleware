/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import java.util.Collection;

/**
 * Just a little interface that a bootstrap class
 * should implement.  
 */
public interface LittleBootstrap {
    /**
     * Bootstrap a littleware environment - assumes registered modules
     * launch processing
     */
    public void bootstrap ();

    /**
     * Boot the littleware runtime and return a guice-injected
     * instance of the given class.
     *
     * @param bootClass to instantiate
     * @return injected object upon system startup
     */
    public <T> T bootstrap( Class<T> bootClass );
    
    /**
     * Startup the application-scope runtime if not already done so, and instantiate
     * a SessionBootstrap.SessionBuilder object
     * 
     * @return builder with which to start a user session
     */
    public SessionBootstrap.SessionBuilder  newSessionBuilder();
    

    /**
     * Shutdown the littleware component associated with this object.
     */
    public void shutdown();

    /**
     * Bootstrap plugins
     */
    public Collection<? extends LittleModule> getModuleSet();

    public interface Builder {
        public LittleBootstrap build();
    }

}
