/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import littleware.base.feedback.Feedback;
import littleware.bootstrap.LittleBootstrap;


@Singleton
public class ConfirmShutdownHandler implements ShutdownHandler {

    private final LittleBootstrap bootstrap;
    private final Feedback fb;
    private boolean shuttingDown = false;

    @Inject
    public ConfirmShutdownHandler(LittleBootstrap bootstrap,
            Feedback fb) {
        this.bootstrap = bootstrap;
        this.fb = fb;
    }

    @Override
    public void requestShutdown() {
        SwingUtilities.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        if ( shuttingDown ) {
                            fb.info( "Shutdown already in progress ..." );
                            return;
                        }
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Really exit?", "Really exit?",
                                JOptionPane.YES_NO_OPTION)) {
                            // kick off to a non-swing, non-osgi-executor thread to issue the shutdown
                            new Thread(new Runnable() {

                        @Override
                                public void run() {
                                    fb.info("Shutting down - please wait ...");
                                    shuttingDown = true;
                                    bootstrap.shutdown();
                                    System.exit(0);
                                }
                            }).start();
                        }
                    }
                });
    }
}
