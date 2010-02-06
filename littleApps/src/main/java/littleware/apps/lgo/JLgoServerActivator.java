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
import java.awt.event.ActionEvent;
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
import littleware.security.auth.LittleBootstrap;
import org.osgi.framework.BundleContext;

/**
 * Specialization of LgoServerActivator registers the application
 * as a SingletonService with JNLP
 */
public class JLgoServerActivator extends LgoServerActivator {

    private static final Logger log = Logger.getLogger(JLgoServerActivator.class.getName());
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
    public JLgoServerActivator(LgoServer.ServerBuilder serverBuilder,
            LittleBootstrap bootstrap) {
        super(serverBuilder, bootstrap);
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
                        new JLabel("<html>n9n server running: http://localhost:9898/n9n/lgo/help</html>")).newRow();
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
