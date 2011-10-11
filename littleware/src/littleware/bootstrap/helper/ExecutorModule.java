/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.helper;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Littleware module binds, starts, and shuts down ExecutorService
 * thread pool.
 */
public class ExecutorModule extends AbstractAppModule {
    
    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new ExecutorModule(profile);
        }
    }

    private ExecutorModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    /**
     * Utility activator takes care of shutting down the
     * executor service and the JCS cache, and bootstraps
     * the session helper.
     * Public for guice-no_aop access only.
     */
    public static class ExecActivator implements BundleActivator {

        private final ExecutorService executor;
        private final ScheduledExecutorService scheduledExecutor;

        @Inject
        public ExecActivator(ExecutorService executor,
                ScheduledExecutorService scheduledExecutor) {
            this.executor = executor;
            this.scheduledExecutor = scheduledExecutor;
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }

        }
    }

    @Override
    public void configure(Binder binder) {
        final ScheduledExecutorService schedExec = Executors.newScheduledThreadPool(2);
        final ExecutorService exec = Executors.newFixedThreadPool(10);
        binder.bind(ExecutorService.class).toInstance( exec );
        binder.bind(ScheduledExecutorService.class).toInstance( schedExec );
    }

    @Override
    public Class<ExecActivator> getActivator() {
        return ExecActivator.class;
    }
}
