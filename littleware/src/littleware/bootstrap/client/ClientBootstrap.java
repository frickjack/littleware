/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap.client;

import com.google.inject.Provider;
import java.util.Collection;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.bootstrap.LittleBootstrap;
import littleware.security.auth.SessionHelper;

/**
 * Client mode bootstrap.
 * Loads ClientModule and AppModule service listeners.
 */
public interface ClientBootstrap extends AppBootstrap {

    @Override
    public Collection<? extends ClientModule> getModuleSet();

    public interface LoginSetup {
        public ClientBootstrap helper( SessionHelper value );
        public ClientBootstrap login( LoginContext context ) throws LoginException;
        public ClientBootstrap subject( Subject subject );
        public ClientBootstrap automatic() throws LoginException;
        public ClientBootstrap automatic( String name, String password ) throws LoginException;
    }
    
    public interface ClientBuilder {
        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<ClientModuleFactory> getModuleSet();

        public ClientBuilder addModuleFactory(ClientModuleFactory factory);

        public ClientBuilder removeModuleFactory(ClientModuleFactory factory);

        public ClientBuilder profile( AppProfile config );

        public LoginSetup build();
    }

    public static final Provider<ClientBuilder> clientProvider = new Provider<ClientBuilder>() {
        @Override
        public ClientBuilder get() {
            return new SimpleClientBuilder();
        }
    };

}
