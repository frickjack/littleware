/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.bootstrap;

import java.util.Collection;


/**
 * Bootstrap a littleware cilent/server session.
 * <ul>
 * <li> First, launch a standard AppBootstrap application </li>
 * <li> Next, inject a ClientBootstrap.ClientBuilder to setup one or more sessions </li>
 * </ul>
 */
public interface ClientBootstrap {

    
    public interface ClientBuilder {
        public Collection<SessionModuleFactory> getSessionModuleSet();
        public ClientBuilder addModuleFactory(SessionModuleFactory factory);
        public ClientBuilder removeModuleFactory(SessionModuleFactory factory);

        public ClientBootstrap build();
        
    }

    /**
     * Read-only view of registered session modules
     */
    public Collection<SessionModule> getSessionModuleSet();


    /**
     * Start a new session with access to session-specific services
     * like SessionManager and LoginManager.
     * Note that this method configures an unauthenticated session -
     * many littleware services require an authenticated session.
     * Register a listener with the LoginManager to authenticate on demand.
     *
     * @param clazz the class to instantiate and inject
     * @return session-injected instance of clazz for an unauthenticated session
     * @exception IllegalStateException if bootstrap has not yet been called
     */
    public <T> T  startSession( Class<T> clazz );

    /**
     * Convenience method authenticates as the test-user with the
     * littleware client backend.
     */
    public <T> T  startTestSession( Class<T> clazz );

}
