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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AbstractLittleBootstrap;
import littleware.bootstrap.client.ClientBootstrap.ClientBuilder;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.ClientModule.ClientFactory;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.client.ClientLoginModule;

public class SimpleClientBuilder implements ClientBootstrap.ClientBuilder {

    private static final Logger log = Logger.getLogger(SimpleClientBuilder.class.getName());
    private final List<ClientModule.ClientFactory> factoryList = new ArrayList<ClientModule.ClientFactory>();
    private AppProfile profile;

    {
        for (ClientModule.ClientFactory moduleFactory : ServiceLoader.load(ClientModule.ClientFactory.class)) {
            factoryList.add(moduleFactory);
        }
    }

    @Override
    public Collection<ClientFactory> getModuleList() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public ClientBuilder addModuleFactory(ClientFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public ClientBuilder removeModuleFactory(ClientFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public ClientBuilder profile(AppProfile value) {
        this.profile = value;
        return this;
    }

    private static class Bootstrap extends AbstractLittleBootstrap implements ClientBootstrap {

        public Bootstrap(Collection<? extends ClientModule> moduleSet) {
            super(moduleSet);
        }

        @Override
        public <T> T bootstrap(Class<T> injectTarget) {
            final CallbackHandler callbackHandler = null;

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
                return bootstrap(injectTarget, context);
            } catch (LoginException ex) {
                throw new IllegalStateException("Login failed", ex);
            }
        }

        @Override
        public <T> T bootstrap(Class<T> injectTarget, SessionHelper helper) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T bootstrap(Class<T> injectTarget, LoginContext loginContext) throws LoginException {
            loginContext.login();
            return bootstrap( injectTarget, loginContext.getSubject() );
        }

        @Override
        public <T> T bootstrap(Class<T> injectTarget, Subject subject) {
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
            return bootstrap(injectTarget, helper);
        }
    }

    @Override
    public ClientBootstrap build() {
        final ImmutableList.Builder<ClientModule> builder = ImmutableList.builder();
        for (ClientModule.ClientFactory factory : factoryList) {
            builder.add(factory.build(profile));
        }
        return new Bootstrap(builder.build());
    }
}
