/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.demo.simpleCL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.ImmutableList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import littleware.apps.client.ClientBootstrap;
import littleware.security.auth.LittleBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * Command-line application launcher
 */
public class CLApp implements BundleActivator {
    private static final Logger log = Logger.getLogger( CLApp.class.getName() );

    private static List<String>  argv;
    private boolean running = false;
    
    private final LittleBootstrap bootstrap;
    private final ExecutorService executor;
    private final Provider<SimpleCLBuilder> provideSimpleCL;

    private static class Runner implements Runnable {
        private final Callable<String> app;
        private final LittleBootstrap bootstrap;

        public Runner( LittleBootstrap bootstrap, Callable<String> app ) {
            this.app = app;
            this.bootstrap = bootstrap;
        }

        @Override
        public void run() {
            try {
                System.out.println( app.call() );
            } catch ( Exception ex ) {
                System.err.print( "Caught exception: " );
                ex.printStackTrace( System.err );
            } finally {
                bootstrap.shutdown();
            }
        }
    }

    @Inject
    public CLApp( 
            LittleBootstrap bootstrap,
            ExecutorService executor,
            Provider<SimpleCLBuilder> provideSimpleCL
            ) {
        this.bootstrap = bootstrap;
        this.executor = executor;
        this.provideSimpleCL = provideSimpleCL;
    }

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
                        executor.submit( new Runner( bootstrap, provideSimpleCL.get().argv( argv ).build() ));
                        // launch onto dispatch thread
                        //SwingUtilities.invokeLater( LgoCommandLine.this );
                        //new Thread(LgoCommandLine.this).start();
                    }
                }
            });
        }
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        
    }

    public static void main( String[] argv ) {
        CLApp.argv = ImmutableList.of( argv );
        final ClientBootstrap bootstrap = new ClientBootstrap();
        bootstrap.getOSGiActivator().add( CLApp.class );
        bootstrap.bootstrap();
    }
}
