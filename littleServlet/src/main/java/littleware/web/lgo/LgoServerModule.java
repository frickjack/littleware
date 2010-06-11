/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.lgo;

import com.google.inject.Inject;
import java.awt.event.ActionEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import littleware.base.Maybe;
import littleware.base.swing.GridBagWrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.client.AbstractClientModule;
import littleware.bootstrap.client.AppBootstrap;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.ClientModule;
import littleware.bootstrap.client.ClientModuleFactory;
import org.joda.time.DateTime;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class LgoServerModule extends AbstractClientModule {

    private static final Logger log = Logger.getLogger(LgoServerModule.class.getName());

    private LgoServerModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public Class<? extends Activator> getActivator() {
        if ( this.getProfile().equals( AppProfile.JNDI )) {
            return JNDIActivator.class;
        } else {
            return Activator.class;
        }
    }

    
    //-------------------

    public static class Factory implements ClientModuleFactory {

        public ClientModule build(AppProfile profile) {
            return new LgoServerModule( profile );
        }
    }

    //-------------------

    public static class Activator implements BundleActivator {

        private final LgoServer.ServerBuilder serverBuilder;
        private Maybe<LgoServer> maybeServer = Maybe.empty();
        private Maybe<? extends ScheduledFuture<?>> maybeFuture = Maybe.empty();
        private final ScheduledExecutorService executor;
        private final LittleBootstrap bootstrap;

        @Inject
        public Activator(
                LgoServer.ServerBuilder serverBuilder,
                LittleBootstrap bootstrap,
                ScheduledExecutorService executor
                )
        {
            this.serverBuilder = serverBuilder;
            this.bootstrap = bootstrap;
            this.executor = executor;
        }

        @Override
        public void start(BundleContext bc) throws Exception {
            maybeServer = Maybe.something(serverBuilder.launch());
            final DateTime now = new DateTime();
            final DateTime tomorrow = now.plusDays(1).minusHours(now.getHourOfDay() - 1);
            log.log(Level.INFO, "Scheduling auto-shutdown for " + tomorrow);
            maybeFuture = Maybe.something(executor.schedule(new Runnable() {

                @Override
                public void run() {
                    try {
                        log.log(Level.INFO, "Running automatic shutdown");
                        maybeFuture = Maybe.empty();
                        bootstrap.shutdown();
                        // give everything a little extra time to shut down
                        Thread.sleep(10000);
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Unexpected exception on auto-shutdown", ex);
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
            executor.schedule(new Runnable() {

                @Override
                public void run() {
                    log.log(Level.INFO, "Exiting application ...");
                    System.exit(0);
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    //-------------------
    
    /**
     * Specialization of Activator registers the application
     * as a SingletonService with JNLP
     */
    public static class JNDIActivator extends Activator {

        private Maybe<SingleInstanceService> maybeService = Maybe.empty();
        private JFrame jmessage = null;
        private SingleInstanceListener listener = new SingleInstanceListener() {

            @Override
            public void newActivation(String[] strings) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        jmessage.setVisible(true);
                    }
                });

            }
        };

        @Inject
        public JNDIActivator(
                LgoServer.ServerBuilder serverBuilder,
                LittleBootstrap bootstrap,
                ScheduledExecutorService executor
                ) {
            super(serverBuilder, bootstrap, executor);
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
            super.start(ctx);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    jmessage = new JFrame();
                    final GridBagWrap gb = GridBagWrap.wrap(jmessage);
                    gb.remainderX().anchorCenter().gridheight(4).fillBoth().add(
                            new JLabel("<html>n9n server running: http://localhost:"
                            + JettyServerBuilder.serverPort + "/n9n/lgo/help</html>")).newRow();
                    gb.fillNone().gridwidth(1).gridheight(1).add(
                            new JButton(new AbstractAction("Ok") {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            jmessage.setVisible(false);
                        }
                    }));
                    jmessage.pack();
                    jmessage.setVisible(true);
                }
            });

            try {
                maybeService = Maybe.something(
                        (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService"));
                maybeService.get().addSingleInstanceListener(listener);
            } catch (UnavailableServiceException ex) {
                log.log(Level.WARNING, "Failed to register as SingleInstanceService with JNLP", ex);
            }
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            if (maybeService.isSet()) {
                maybeService.get().removeSingleInstanceListener(listener);
                maybeService = Maybe.empty();
            }
            jmessage.dispose();
            super.stop(ctx);
        }
    }
}
