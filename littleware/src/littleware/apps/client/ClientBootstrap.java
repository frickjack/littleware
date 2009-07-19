/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import littleware.apps.lgo.EzModule;
import littleware.base.Maybe;
import littleware.security.auth.AbstractGOBootstrap;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.client.ClientCache;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Client side OSGi bootstrap with Guice injection -
 * very similar to server side implementation.
 */
public class ClientBootstrap extends AbstractGOBootstrap {

    private static final Logger olog = Logger.getLogger(ClientBootstrap.class.getName());

    /** 
     * Utility activator takes care of shutting down the
     * executor service.  Public for guice-no_aop access only.
     */
    public static class Activator implements BundleActivator {

        private final ExecutorService executor;

        @Inject
        public Activator(ExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void start(BundleContext arg0) throws Exception {
        }

        @Override
        public void stop(BundleContext arg0) throws Exception {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    private final ClientServiceGuice clientGuice;
    /**
     * Property specifies the server host to authenticate to
     * if authentication is necessary.  Actually accesses
     * ClientServiceGuice host property.
     */
    public Maybe<String> getHost() {
        return clientGuice.getHost();
    }

    public void setHost(Maybe<String> host) {
        clientGuice.setHost( host );
    }


    /**
     * Internal shared constructor
     */
    private ClientBootstrap( ClientServiceGuice clientGuice ) {
        super(
                Arrays.asList(
                new EzModule(),
                new littleware.apps.swingclient.StandardSwingGuice(),
                new littleware.apps.client.StandardClientGuice(),
                new littleware.apps.misc.StandardMiscGuice(),
                clientGuice ),
                new ArrayList<Class<? extends BundleActivator>>(),
                false);
        this.getGuiceModule().add(
                new Module() {

                    @Override
                    public void configure(Binder binder) {
                        binder.bind(LittleBootstrap.class).toInstance(ClientBootstrap.this);
                    }
                });
        this.getOSGiActivator().add(AssetModelServiceListener.class);
        this.getOSGiActivator().add(ClientCache.class);
        this.getOSGiActivator().add(Activator.class);
        this.clientGuice = clientGuice;
    }

    /**
     * Allow override of default remote server host
     *
     * @param sHost to connect to
     */
    public ClientBootstrap( String sHost ) {
        this(new ClientServiceGuice( sHost ));
    }

    public ClientBootstrap() {
        this(new ClientServiceGuice());
    }
}
