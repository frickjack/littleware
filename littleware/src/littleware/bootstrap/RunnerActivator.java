/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import littleware.base.Maybe;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * Abstract OSGi activator that starts the run() method
 * implemented by subtypes on a new thread upon receiving
 * a "framework started" event from the OSGi runtime on
 * the listener registered at activator start() time.
 * The activator cancels the child's execution on activator stop().
 */
public abstract class RunnerActivator implements BundleActivator, Runnable {
    private boolean running = false;
    Maybe<Future<Object>>  maybeFuture = Maybe.empty();
    Maybe<ExecutorService> maybeExecutor = Maybe.empty();

    /** Launch worker thread once OSGi has started */
    @Override
    public void start(final BundleContext ctx) throws Exception {
        if (!running) {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED) && (!running)) {
                        running = true;
                        ctx.removeFrameworkListener(this);
                        // launch onto dispatch thread
                        //SwingUtilities.invokeLater( LgoCommandLine.this );
                        maybeExecutor = Maybe.something(
                                Executors.newFixedThreadPool(1)
                                );
                        maybeFuture = Maybe.something( maybeExecutor.get().submit( Executors.callable( RunnerActivator.this ) ) );
                    }
                }
            });
        }
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        if ( maybeFuture.isSet() && (! maybeFuture.get().isDone() ) ) {
            maybeFuture.get().cancel(true);
            maybeFuture = Maybe.empty();
        }
        if ( maybeExecutor.isSet() ) {
            maybeExecutor.get().shutdown();
            maybeExecutor = Maybe.empty();
        }
    }
}
