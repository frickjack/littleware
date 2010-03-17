/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import littleware.asset.client.LittleService;
import littleware.security.auth.AbstractGOBootstrap;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bootstrap boots a non-littleware runtime environment.
 * Binds ExecutorService to a 4-thread pool.
 */
public class NullBootstrap extends AbstractGOBootstrap {
    /**
     * Utility activator takes care of shutting down the
     * executor service and the JCS cache, and bootstraps
     * the session helper.
     * Public for guice-no_aop access only.
     */
    public static class ExecActivator implements BundleActivator {

        private final ExecutorService executor;

        @Inject
        public ExecActivator(ExecutorService executor ) {
            this.executor = executor;
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
            
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
    
    {
        this.getGuiceModule().add( new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(4));
            }
        } );
        this.getOSGiActivator().add(ExecActivator.class);
    }

}
