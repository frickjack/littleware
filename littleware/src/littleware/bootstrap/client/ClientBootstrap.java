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
        public ClientBuilder helper( SessionHelper value );
        public ClientBuilder login( LoginContext context ) throws LoginException;
        public ClientBuilder subject( Subject subject );
        public ClientBuilder automatic() throws LoginException;
    }
    
    public interface ClientBuilder extends LittleBootstrap.Builder {
        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<ClientModule.ClientFactory> getModuleList();

        public ClientBuilder addModuleFactory(ClientModule.ClientFactory factory);

        public ClientBuilder removeModuleFactory(ClientModule.ClientFactory factory);

        public ClientBuilder profile( AppProfile config );

        @Override
        public ClientBootstrap build();
    }

    public static final Provider<LoginSetup> provider = new Provider<LoginSetup>() {
        @Override
        public LoginSetup get() {
            return new SimpleLoginSetup();
        }
    };

}
