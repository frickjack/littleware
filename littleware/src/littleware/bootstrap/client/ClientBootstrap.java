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
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.security.auth.SessionHelper;

/**
 * Client mode bootstrap.
 * Loads ClientModule and AppModule service listeners.
 */
public interface ClientBootstrap extends AppBootstrap {

    @Override
    public Collection<? extends ClientModule> getModuleSet();

    public interface LoginSetup {
        public static String  TestUserName = "littleware.test_user";
        
        public ClientBootstrap helper( SessionHelper value );
        /**
         * Attempt to login with given login context
         */
        public ClientBootstrap login( LoginContext context ) throws LoginException;
        /**
         * Attempt login with given name and password and the
         * ClientLoginModule login configuration
         */
        public ClientBootstrap login( String name, String password
                ) throws LoginException;

        /**
         * Authenticate as the test user to run regression tests.
         * Throws runtime exception on failure to authenticate.
         */
        public ClientBootstrap test();
        public ClientBootstrap test( Configuration loginConfig );

        /**
         * Attempt login with given login configuration, name, and password
         */
        public ClientBootstrap login( Configuration loginConfig, String name, String password
                ) throws LoginException;

        public ClientBootstrap subject( Subject subject );
        /**
         * Auto-retry login with littleware.security.auth.ClientLoginModule login configuration
         */
        public ClientBootstrap automatic() throws LoginException;
        /**
         * Auto-retry login with user-supplied login configuration
         */
        public ClientBootstrap automatic( Configuration loginConfig ) throws LoginException;
        /**
         * Auto-retry login with user-supplied login configuration,
         *
         * @param name initial username
         * @param password password guess for first authentication attempt
         */
        public ClientBootstrap automatic( Configuration loginConfig,
                String name, String password ) throws LoginException;
    }
    
    public interface ClientBuilder {
        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<ClientModuleFactory> getModuleSet();

        public ClientBuilder addModuleFactory(ClientModuleFactory factory);

        public ClientBuilder removeModuleFactory(ClientModuleFactory factory);

        public AppProfile getProfile();
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
