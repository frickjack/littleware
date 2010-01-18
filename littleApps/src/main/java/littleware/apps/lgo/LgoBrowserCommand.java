/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.inject.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.ExtendedAssetViewController;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.base.PropertiesGuice;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.LoggerFeedback;

/**
 * Launch a Swing browser, and return the whatever
 */
public class LgoBrowserCommand extends AbstractLgoCommand<String, Maybe<UUID>> {
    private final static Logger log = Logger.getLogger(LgoBrowserCommand.class.getName());
    
    private final AssetSearchManager osearch;
    private final AssetModelLibrary olib;
    private final AssetPathFactory opathFactory;
    private final Provider<JAssetBrowser> provideBrowser;
    private final Provider<ExtendedAssetViewController> provideControl;
    private final Provider<JSimpleAssetToolbar> provideToolbar;
    private final EventBarrier<UUID> barrier = new EventBarrier<UUID>();


    /**
     * Container for the different Swing components
     * that make up a browser frame - including the frame itself.
     * Mostly exists so we can pass data to buildMenuBar() ...
     */
    protected class UIStuff {

        public final JAssetBrowser browser = provideBrowser.get();
        public final JSimpleAssetToolbar toolbar = provideToolbar.get();
        public final JFrame jframe = new JFrame("Asset Browser");

        {
            toolbar.setConnectedView(browser);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.CREATE).setEnabled(true);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.EDIT).setEnabled(true);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.DELETE).setEnabled(true);

            jframe.setLayout(new BorderLayout());
            jframe.add(toolbar, BorderLayout.NORTH);
            jframe.add(browser, BorderLayout.CENTER);
            jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
    }

    /**
     * Inject the browser that this command launches,
     * and the controller to attach to it.
     * Inject providers - Swing widgets will be allocated
     * and initialized on the Swing dispatch thread;
     * the LgoBrowserCommand may be allocated
     * and bootstrap on an lgo shell worker thread.
     * 
     * @param provideBrowser to launch on run
     * @param control to view browser
     * @param provideToolbar to connect to the browser
     */
    @Inject
    public LgoBrowserCommand(
            final Provider<JAssetBrowser> provideBrowser,
            final Provider<ExtendedAssetViewController> provideControl,
            final Provider<JSimpleAssetToolbar> provideToolbar,
            AssetSearchManager search,
            AssetModelLibrary lib,
            AssetPathFactory pathFactory) {
        super(LgoBrowserCommand.class.getName());
        this.provideBrowser = provideBrowser;
        this.provideControl = provideControl;
        this.provideToolbar = provideToolbar;
        osearch = search;
        olib = lib;
        opathFactory = pathFactory;
    }
    private UUID ou_start = UUIDFactory.parseUUID("00000000000000000000000000000000");

    /**
     * Property sets the asset to start browsing at.
     */
    public UUID getStart() {
        return ou_start;
    }

    public void setStart(UUID u_start) {
        ou_start = u_start;
    }

    /**
     * Start browsing 
     * 
     * @param s_start_path reset start UUID to asset at other end of s_start_path
     * @return where the user stops browsing
     */
    @Override
    public Maybe<UUID> runSafe(Feedback feedback, String sPathIn) {
        String sStartPath = sPathIn;
        if (Whatever.get().empty(sStartPath) && (!getArgs().isEmpty())) {
            sStartPath = getArgs().get(0);
        }
        if (null != sStartPath) {
            try {
                final Asset a_start = osearch.getAssetAtPath(opathFactory.createPath(sStartPath)).get();
                olib.syncAsset(a_start);
                setStart(a_start.getId());
            } catch (Exception e) {
                feedback.log(Level.WARNING, "Unable to load asset at path: " + sStartPath + ", caught: " + e);
            }
        }
        Maybe<UUID> maybeResult = Maybe.empty();
        if (SwingUtilities.isEventDispatchThread()) {
            createGUI(Maybe.emptyIfNull(getStart()));
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    createGUI(Maybe.emptyIfNull(getStart()));
                }
            });
            try {
                maybeResult = Maybe.emptyIfNull(barrier.waitForEventData());
            } catch (InterruptedException ex) {
                feedback.info("Interrupted waiting for browser result: " + ex);
                log.log(Level.WARNING, "Caught exception", ex);
            }
        }
        return maybeResult;
    }

    /**
     * Allow subtypes to specialize the menu
     *
     * @return menubar to attach to the browser frame
     */
    protected JMenuBar buildMenuBar(final UIStuff stuff) {
        final JMenuBar jBar = new JMenuBar();
        final JMenu jMenuFile = new JMenu("File");
        final JMenuItem jNewBrowser = new JMenuItem(
                new AbstractAction("New Browser") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        createGUI(Maybe.emptyIfNull(stuff.browser.getAssetModel().getAsset().getId()));
                    }
                });

        final JMenuItem jQuit = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                stuff.jframe.setVisible( false );
                stuff.jframe.dispose();
            }
        });

        jNewBrowser.setMnemonic(KeyEvent.VK_N);
        jQuit.setMnemonic(KeyEvent.VK_X);

        jMenuFile.add( jNewBrowser );
        jMenuFile.add(jQuit);
        jMenuFile.setMnemonic(KeyEvent.VK_F);
        jBar.add(jMenuFile);

        return jBar;
    }
    boolean bFirstWindow = true;

    private void createGUI(Maybe<UUID> maybeStart) {
        final ExtendedAssetViewController control = provideControl.get();
        final UIStuff stuff = new UIStuff();

        control.setControlView(stuff.browser);
        stuff.toolbar.addLittleListener(control);
        if (maybeStart.isSet()) {
            try {
                final Maybe<AssetModel> maybeModel = olib.retrieveAssetModel(maybeStart.get(), osearch);
                if ( maybeModel.isSet() ) {
                    stuff.browser.setAssetModel( maybeModel.get() );
                } else {
                    log.log( Level.INFO, "Requested initial asset model does not exist: " + getStart() );
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Failed to load initial asset model " + getStart(), e);
            }
        }

        if (bFirstWindow) {
            bFirstWindow = false;
            final JButton jButtonSelect = new JButton("Select");
            jButtonSelect.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    barrier.publishEventData(stuff.browser.getAssetModel().getAsset().getId());
                    stuff.jframe.dispose();
                }
            });
            final JPanel jPanelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            jPanelButton.add(jButtonSelect);
            stuff.jframe.add(jPanelButton, BorderLayout.PAGE_END);

            stuff.jframe.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent ev) {
                    if (!barrier.isDataReady()) {
                        barrier.publishEventData(null);
                    }

                }

                @Override
                public void windowClosed(WindowEvent e) {
                    if (!barrier.isDataReady()) {
                        barrier.publishEventData(null);
                    }

                }
            });
            stuff.jframe.setJMenuBar(buildMenuBar(stuff));
        }

        stuff.jframe.pack();
        stuff.jframe.setVisible( true );
    }


    /**
     * Launch a browser with the session
     * loaded from littleware.properties.
     *
     * @param v_args command-line args
     * @TODO setup standard feedback mechanism in StandardSwingGuice
     */
    public static void main(String[] v_args) {
        try {
            final Injector injector = Guice.createInjector(new Module[]{
                        new EzModule(),
                        new littleware.apps.swingclient.StandardSwingGuice(),
                        new littleware.apps.client.StandardClientGuice(),
                        new littleware.apps.misc.StandardMiscGuice(),
                        new littleware.security.auth.ClientServiceGuice(),
                        new PropertiesGuice(littleware.base.PropertiesLoader.get().loadProperties())
                    });
            Feedback feedback = new LoggerFeedback();
            LgoBrowserCommand command = injector.getInstance(LgoBrowserCommand.class);
            command.runCommand(feedback,
                    "/littleware.home/");
        } catch (final Exception ex) {
            log.log(Level.SEVERE, "Failed command, caught: " + ex, ex);
            try {
                final Runnable runner = new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Launch failed", JOptionPane.ERROR_MESSAGE);
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    runner.run();
                } else {
                    log.log(Level.INFO, "Waiting for user input ...");
                    SwingUtilities.invokeAndWait(runner);
                }

            } catch (Exception ex1) {
                log.log(Level.SEVERE, null, ex1);
            }

            System.exit(1);
        }
    }
}
