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

import littleware.asset.client.bootstrap.internal.SimpleClientBuilder;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.AppBootstrap;
import com.google.inject.Provider;
import java.util.Collection;


/**
 * Client mode bootstrap.
 * Loads ClientModule and AppModule service listeners.
 */
public interface ClientBootstrap extends AppBootstrap {

    
    public interface ClientBuilder {
        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<AppModuleFactory> getModuleSet();
        public ClientBuilder addModuleFactory(AppModuleFactory factory);
        public ClientBuilder removeModuleFactory(AppModuleFactory factory);

        public Collection<SessionModuleFactory> getSessionModuleSet();
        public ClientBuilder addModuleFactory(SessionModuleFactory factory);
        public ClientBuilder removeModuleFactory(SessionModuleFactory factory);

        public AppProfile getProfile();
        public ClientBuilder profile( AppProfile config );

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
     * @param sessionId unique id for the new session
     * @return session-injected instance of clazz for an unauthenticated session
     * @exception IllegalStateException if bootstrap has not yet been called
     */
    public <T> T  startSession( Class<T> clazz, String sessionId );

    /**
     * Convenience method authenticates as the test-user with the
     * littleware client backend.
     */
    public <T> T  startTestSession( Class<T> clazz, String sessionId );

    public static final Provider<ClientBuilder> clientProvider = new Provider<ClientBuilder>() {
        @Override
        public ClientBuilder get() {
            return new SimpleClientBuilder();
        }
    };

}
