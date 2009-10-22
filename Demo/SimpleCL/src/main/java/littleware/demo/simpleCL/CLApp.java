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
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import littleware.asset.AssetSearchManager;
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
    private boolean running = false;
    private final AssetSearchManager search;
    private final LittleBootstrap bootstrap;

    @Inject
    public CLApp( AssetSearchManager search,
            LittleBootstrap bootstrap,
            ExecutorService executor
            ) {
        this.search = search;
        this.bootstrap = bootstrap;
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
                        // launch onto dispatch thread
                        //SwingUtilities.invokeLater( LgoCommandLine.this );
                        //new Thread(LgoCommandLine.this).start();
                    }
                }
            });
        }
    }

    public void stop(BundleContext ctx) throws Exception {
        
    }
}
