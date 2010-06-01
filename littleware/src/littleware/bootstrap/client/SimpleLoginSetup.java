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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.base.AssertionFailedException;
import littleware.base.Whatever;
import littleware.bootstrap.client.ClientBootstrap.ClientBuilder;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SimpleCallbackHandler;
import littleware.security.auth.client.ClientLoginModule;

public class SimpleLoginSetup implements ClientBootstrap.LoginSetup {
    private final SimpleClientBuilder builder;

    public SimpleLoginSetup( SimpleClientBuilder builder ) {
        this.builder = builder;
    }

    @Override
    public ClientBootstrap helper(SessionHelper value) {
        return builder.build( value );
    }

    @Override
    public ClientBootstrap login(LoginContext context) throws LoginException {
        context.login();
        return subject(context.getSubject());
    }

    @Override
    public ClientBootstrap subject(Subject subject) {
        SessionHelper helper = null;
        for (Object scan : subject.getPrivateCredentials()) {
            if (scan instanceof SessionHelper) {
                helper = (SessionHelper) scan;
                break;
            }
        }
        if (null == helper) {
            throw new IllegalArgumentException("Subject does not include SessionHelper in private credential set");
        }
        return helper(helper);
    }

    @Override
    public ClientBootstrap automatic() throws LoginException {
        final String user = System.getProperty( "user.name" );
        // throw in some retry logic, etc.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    public ClientBootstrap automatic(String name, String password) throws LoginException {
        final CallbackHandler callbackHandler = new SimpleCallbackHandler(name, password);

        LoginContext context;
        try {
            context = new LoginContext("littleware.login", new Subject(), callbackHandler);
        } catch (LoginException ex) {
            try {
                context = new LoginContext("littleware.login", new Subject(), callbackHandler, ClientLoginModule.newBuilder().build());
            } catch (LoginException ex2) {
                throw new AssertionFailedException("Failed to setup LoginContext", ex);
            }
        }
        try {
            return login(context);
        } catch (LoginException ex) {
            throw new IllegalStateException("Login failed", ex);
        }
    }
}
