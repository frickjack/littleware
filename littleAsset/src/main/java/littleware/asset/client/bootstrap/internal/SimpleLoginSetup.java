/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.bootstrap.internal;

import littleware.bootstrap.AppBootstrap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.base.AssertionFailedException;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SimpleCallbackHandler;
import littleware.security.auth.client.CliCallbackHandler;
import littleware.security.auth.client.ClientLoginModule;
import littleware.security.auth.client.JPasswordDialog;

public class SimpleLoginSetup implements ClientBootstrap.LoginSetup {

    private static final Logger log = Logger.getLogger(SimpleLoginSetup.class.getName());
    public static final String ConfigName = "littleware.login";
    private final SimpleClientBuilder builder;

    public SimpleLoginSetup(SimpleClientBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ClientBootstrap helper(SessionHelper value) {
        return builder.build(value);
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
    public ClientBootstrap test() {
        return test(ClientLoginModule.newBuilder().build());
    }

    @Override
    public ClientBootstrap test(Configuration loginConfig) {
        try {
            return login(loginConfig, TestUserName, "");
        } catch (LoginException ex) {
            throw new AssertionFailedException("Failed to login as test user", ex);
        }
    }

    @Override
    public ClientBootstrap login(String name, String password) throws LoginException {
        return login(ClientLoginModule.newBuilder().build(), name, password);
    }

    @Override
    public ClientBootstrap login(Configuration loginConfig, String name, String password) throws LoginException {
        final CallbackHandler callbackHandler = new SimpleCallbackHandler(name, password);

        log.log( Level.FINE, "Attempting to login as user: {0}", name );
        return login(new LoginContext(ConfigName, new Subject(), callbackHandler, loginConfig));
    }

    @Override
    public ClientBootstrap automatic() throws LoginException {
        return automatic(ClientLoginModule.newBuilder().build());
    }

    @Override
    public ClientBootstrap automatic(Configuration loginConfig) throws LoginException {
        final String user = System.getProperty("user.name");
        return automatic(loginConfig, user, "");
    }

    @Override
    public ClientBootstrap automatic(Configuration loginConfig, String name, String password) throws LoginException {
        // first - try to login with the initial user-name/password guess
        try {
            return login(loginConfig, name, password);
        } catch (LoginException ex) {
            log.log(Level.FINE, "Failed to authenticate with initial password guess for " + name );
        }
        final AppBootstrap.AppProfile profile = builder.getProfile();

        // give the interactive user 3 guesses
        for (int i = 0; true; ++i) {
            final String message = (i > 0) ? "Failed attempt " + i + ", try again" : "";
            final CallbackHandler handler;
            if (profile.equals(AppBootstrap.AppProfile.CliApp)) {
                handler = new CliCallbackHandler(message);
            } else {
                handler = new JPasswordDialog(name, "",
                        message);
            }
            try {
                return login(new LoginContext(ConfigName, new Subject(), handler, loginConfig));
            } catch (FailedLoginException ex) {
                if (i > 1) {
                    throw ex;
                }
            }
        }
    }
}
