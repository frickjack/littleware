/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.Maybe;
import littleware.bootstrap.LittleBootstrap;
import org.joda.time.DateTime;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LgoServerActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(LgoServerActivator.class.getName());
    private final LgoServer.ServerBuilder serverBuilder;
    private Maybe<LgoServer> maybeServer = Maybe.empty();
    private Maybe<? extends ScheduledFuture<?>> maybeFuture = Maybe.empty();
    private final ScheduledExecutorService executor;
    private final LittleBootstrap bootstrap;

    @Inject
    public LgoServerActivator(LgoServer.ServerBuilder serverBuilder,
            LittleBootstrap bootstrap, ScheduledExecutorService executor ) {
        this.serverBuilder = serverBuilder;
        this.bootstrap = bootstrap;
        this.executor = executor;
    }

    @Override
    public void start(BundleContext bc) throws Exception {
        maybeServer = Maybe.something(serverBuilder.launch());
        final DateTime now = new DateTime();
        final DateTime tomorrow = now.plusDays(1).minusHours(now.getHourOfDay() - 1);
        log.log( Level.INFO, "Scheduling auto-shutdown for " + tomorrow );
        maybeFuture = Maybe.something(executor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    log.log(Level.INFO, "Running automatic shutdown");
                    maybeFuture = Maybe.empty();
                    bootstrap.shutdown();
                    // give everything a little extra time to shut down
                    Thread.sleep( 10000 );
                } catch ( Exception ex ) {
                    log.log( Level.WARNING, "Unexpected exception on auto-shutdown", ex );
                } finally {
                    System.exit(0);
                }
            }
        },
                tomorrow.getMillis() - now.getMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        if (maybeServer.isSet()) {
            try {
                maybeServer.get().shutdown();
            } finally {
                maybeServer = Maybe.empty();
            }
        }
        if (maybeFuture.isSet()) {
            maybeFuture.get().cancel(false);
            maybeFuture = Maybe.empty();
        }
        executor.schedule( new Runnable() {

            @Override
            public void run() {
                log.log( Level.INFO, "Exiting application ..." );
                System.exit( 0 );
            }
        }, 5, TimeUnit.SECONDS );
    }
}
