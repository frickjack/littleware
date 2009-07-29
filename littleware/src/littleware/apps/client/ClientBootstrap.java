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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import littleware.apps.lgo.EzModule;
import littleware.asset.client.LittleService;
import littleware.base.Maybe;
import littleware.security.auth.AbstractGOBootstrap;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.client.CacheActivator;
//import org.apache.jcs.JCS;
//import org.apache.jcs.access.exception.CacheException;
//import org.apache.jcs.engine.control.CompositeCacheManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Client side OSGi bootstrap with Guice injection -
 * very similar to server side implementation.
 */
public class ClientBootstrap extends AbstractGOBootstrap {

    private static final Logger log = Logger.getLogger(ClientBootstrap.class.getName());

    /** 
     * Utility activator takes care of shutting down the
     * executor service and the JCS cache, and bootstraps
     * the session helper.
     * Public for guice-no_aop access only.
     */
    public static class Activator implements BundleActivator {
        private final ExecutorService executor;
        private final SessionHelper helper;
        //private final CompositeCacheManager cacheManager;

        @Inject
        public Activator(ExecutorService executor, SessionHelper helper ) { //, CompositeCacheManager cacheManager) {
            this.executor = executor;
            this.helper = helper;
            //this.cacheManager = cacheManager;
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
            ((LittleService) helper).start( ctx );
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            //cacheManager.shutDown();
            ((LittleService) helper).stop( ctx );
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * Utility Guice module binds JCS cache manager
     * and LittleBootstrap
     */
    private static class CacheModule implements Module {

        @Override
        public void configure(Binder binder) {
            /*..
            // Setup CompositeCacheManager
            final CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();
            final PropertiesLoader propLoader = PropertiesLoader.get();
            final Properties props;
            try {
                props = propLoader.loadProperties("littlecache.properties");
            } catch (IOException ex) {
                throw new AssertionFailedException("Failed to load cache properties", ex);
            }
            final String cachePath = propLoader.getLittleHome().toString() + "/jcsCache";
            props.setProperty("jcs.auxiliary.DC.attributes.DiskPath", cachePath);
            manager.configure(props);
            binder.bind(CompositeCacheManager.class).toInstance(manager);
            try {
                binder.bind(JCS.class).toInstance(JCS.getInstance("littleware"));
            } catch (CacheException ex) {
                throw new AssertionFailedException( "Failed to initialize JCS", ex );
            }
             */
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
        clientGuice.setHost(host);
    }

    /**
     * Setup bootstrap with preconfigured ClientServiceGuice module.
     */
    public ClientBootstrap(ClientServiceGuice clientGuice) {
        super(
                Arrays.asList(
                new EzModule(),
                new littleware.apps.swingclient.StandardSwingGuice(),
                new littleware.apps.client.StandardClientGuice(),
                new littleware.apps.misc.StandardMiscGuice(),
                clientGuice),
                new ArrayList<Class<? extends BundleActivator>>(),
                false
                );
        this.getGuiceModule().add(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(LittleBootstrap.class).toInstance(ClientBootstrap.this);
            }
        });
        //this.getGuiceModule().add(new CacheModule());
        this.getOSGiActivator().add(AssetModelServiceListener.class);
        this.getOSGiActivator().add(CacheActivator.class);
        this.getOSGiActivator().add(Activator.class);
        this.getOSGiActivator().add(SyncWithServer.class );
        this.clientGuice = clientGuice;
    }

    /**
     * Allow override of default remote server host
     *
     * @param sHost to connect to
     */
    public ClientBootstrap(String sHost) {
        this(new ClientServiceGuice(sHost));
    }

    public ClientBootstrap() {
        this(new ClientServiceGuice());
    }
}
