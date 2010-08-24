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

import com.google.common.collect.ImmutableMap;
import java.util.List;
import littleware.lgo.AbstractLgoCommand;
import com.google.inject.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.swingbase.SwingBaseModule;
import littleware.apps.swingbase.view.BaseView;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.ExtendedAssetViewController;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.lgo.AbstractLgoBuilder;
import littleware.lgo.LgoCommand;
import littleware.security.LittleUser;
import littleware.security.auth.client.ClientLoginModule;

/**
 * Launch a Swing browser, and return the whatever
 */
public class LgoBrowserCommand extends AbstractLgoCommand<AssetPath, EventBarrier<Maybe<UUID>>> {

    private final static Logger log = Logger.getLogger(LgoBrowserCommand.class.getName());
    private final Services services;

    public static class Services {

        private final AssetSearchManager search;
        private final AssetModelLibrary assetLib;
        private final AssetPathFactory pathFactory;
        private final Provider<JAssetBrowser> browserProvider;
        private final Provider<ExtendedAssetViewController> provideControl;
        private final Provider<JSimpleAssetToolbar> provideToolbar;
        private final LittleUser user;

        @Inject
        public Services(
                final Provider<JAssetBrowser> provideBrowser,
                final Provider<ExtendedAssetViewController> provideControl,
                final Provider<JSimpleAssetToolbar> provideToolbar,
                AssetSearchManager search,
                AssetModelLibrary lib,
                AssetPathFactory pathFactory,
                LittleUser user) {
            this.browserProvider = provideBrowser;
            this.provideControl = provideControl;
            this.provideToolbar = provideToolbar;
            this.user = user;
            this.search = search;
            this.assetLib = lib;
            this.pathFactory = pathFactory;
        }

        public AssetModelLibrary getAssetLib() {
            return assetLib;
        }

        public AssetPathFactory getPathFactory() {
            return pathFactory;
        }

        public Provider<JAssetBrowser> getBrowser() {
            return browserProvider;
        }

        public Provider<ExtendedAssetViewController> getController() {
            return provideControl;
        }

        public Provider<JSimpleAssetToolbar> getToolbar() {
            return provideToolbar;
        }

        public AssetSearchManager getSearch() {
            return search;
        }

        public LittleUser getUser() {
            return user;
        }
    }

    public static class Builder extends AbstractLgoBuilder<AssetPath> {

        private final Services services;

        @Inject
        public Builder(Services services) {
            super(LgoBrowserCommand.class.getName());
            this.services = services;
        }

        @Override
        public LgoBrowserCommand buildSafe(AssetPath input) {
            return new LgoBrowserCommand(services, input);
        }

        @Override
        public LgoBrowserCommand buildFromArgs(List<String> args) {
            final Map<String,String> argMap = processArgs( args, ImmutableMap.of( "path", "" ) );
            final String pathArg = argMap.get( "path" );
            final AssetPath path;
            if ( Whatever.get().empty(pathArg) ) {
                path = services.getPathFactory().createPath( services.getUser().getId() );
            } else {
                try {
                    path = services.getPathFactory().createPath(pathArg);
                } catch ( BaseException ex ) {
                    throw new IllegalArgumentException( "Unable to parse path: " + pathArg );
                }
            }
            return buildSafe( path );
        }
    }
    private final EventBarrier<Maybe<UUID>> barrier = new EventBarrier<Maybe<UUID>>();

    /**
     * Container for the different Swing components
     * that make up a browser frame - including the frame itself.
     * Mostly exists so we can pass data to buildMenuBar() ...
     */
    protected class UIStuff {

        public final JAssetBrowser browser = services.getBrowser().get();
        public final JSimpleAssetToolbar toolbar = services.getToolbar().get();
        public final JPanel jpanel = new JPanel();
        public final JFrame jframe = new JFrame("Asset Browser");

        {
            toolbar.setConnectedView(browser);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.CREATE).setEnabled(true);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.EDIT).setEnabled(true);
            toolbar.getButton(JSimpleAssetToolbar.ButtonId.DELETE).setEnabled(true);

            jpanel.setLayout(new BorderLayout());
            jpanel.add(toolbar, BorderLayout.NORTH);
            jpanel.add(browser, BorderLayout.CENTER);
            jframe.add(jpanel);
            jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            browser.setAssetModel(services.getAssetLib().syncAsset(services.getUser()));
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
    public LgoBrowserCommand(
            Services services,
            AssetPath input) {
        super(LgoBrowserCommand.class.getName(), input);
        this.services = services;
    }

    /**
     * Start browsing 
     * 
     * @param s_start_path reset start UUID to asset at other end of s_start_path
     * @return where the user stops browsing
     */
    @Override
    public EventBarrier<Maybe<UUID>> runCommand(Feedback feedback) throws Exception {
        final AssetPath startPath = getInput();
        final Asset startAsset = services.getSearch().getAssetAtPath(startPath).get();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createGUI(startAsset);
            }
        });
        /**
         * Do not block command-thread.
         * Can add support for asynchronous results later if needed ...
         *
        try {
        maybeResult = Maybe.emptyIfNull(barrier.waitForEventData());
        } catch (InterruptedException ex) {
        feedback.info("Interrupted waiting for browser result: " + ex);
        log.log(Level.WARNING, "Caught exception", ex);
        }
         */
        return barrier;
    }

    /**
     * Allow hook to construct a new browser panel for swingbase app-launcher ...
     */
    private JPanel buildBrowserPanel() {
        final ExtendedAssetViewController control = services.getController().get();
        final UIStuff stuff = new UIStuff();

        control.setControlView(stuff.browser);
        stuff.toolbar.addLittleListener(control);

        return stuff.jpanel;
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
                        createGUI( stuff.browser.getAssetModel().getAsset() );
                    }
                });

        final JMenuItem jQuit = new JMenuItem(new AbstractAction("Exit") {

            @Override
            public void actionPerformed(ActionEvent e) {
                stuff.jframe.setVisible(false);
                stuff.jframe.dispose();
            }
        });

        jNewBrowser.setMnemonic(KeyEvent.VK_N);
        jQuit.setMnemonic(KeyEvent.VK_X);

        jMenuFile.add(jNewBrowser);
        jMenuFile.add(jQuit);
        jMenuFile.setMnemonic(KeyEvent.VK_F);
        jBar.add(jMenuFile);

        return jBar;
    }
    boolean bFirstWindow = true;

    private void createGUI(Asset startAsset) {
        final ExtendedAssetViewController control = services.getController().get();
        final UIStuff stuff = new UIStuff();

        control.setControlView(stuff.browser);
        stuff.toolbar.addLittleListener(control);


        final AssetModel model = services.getAssetLib().syncAsset(startAsset);
        stuff.browser.setAssetModel(model);


        if (bFirstWindow) {
            bFirstWindow = false;
            final JButton jButtonSelect = new JButton("Select");
            jButtonSelect.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    barrier.publishEventData(
                            Maybe.something(stuff.browser.getAssetModel().getAsset().getId()));
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
                        barrier.publishEventData(Maybe.empty(UUID.class));
                    }
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    if (!barrier.isDataReady()) {
                        barrier.publishEventData(Maybe.empty(UUID.class));
                    }

                }
            });
            stuff.jframe.setJMenuBar(buildMenuBar(stuff));
        }

        stuff.jframe.pack();
        stuff.jframe.setVisible(true);
    }

    /**
     * Only visible for guice - not intended for outside use
     */
    public static class SwingBaseLauncher implements Runnable {

        private final ViewBuilder viewBuilder;
        private final LgoBrowserCommand browserCommand;

        @Inject
        public SwingBaseLauncher(BaseView.ViewBuilder viewBuilder, LgoBrowserCommand browserCommand) {
            this.viewBuilder = viewBuilder;
            this.browserCommand = browserCommand;
        }

        @Override
        public void run() {
            viewBuilder.basicContent(browserCommand.buildBrowserPanel()).build().getContainer().setVisible(true);
        }
    }

    /**
     * Launch a browser with the session
     * loaded from littleware.properties.
     *
     * @param args command-line args
     * @TODO setup standard feedback mechanism in StandardSwingGuice
     */
    public static void main(String[] args) {
        /*... just for testing in serverless environment ...
        {
        // Try to start an internal server for now just for testing
        littleware.bootstrap.server.ServerBootstrap.provider.get().build().bootstrap();
        } */


        try {
            final ClientBootstrap.LoginSetup bootBuilder = ClientBootstrap.clientProvider.get().
                    addModuleFactory(
                    new SwingBaseModule.Factory().appName("littleBrowser").version("2.1").helpUrl(new URL("http://code.google.com/p/littleware/")).properties(new Properties())).build();
            final ClientLoginModule.ConfigurationBuilder loginBuilder = ClientLoginModule.newBuilder();
            // Currently only support -url argument
            if ((args.length > 1) && args[0].matches("^-+[uU][rR][lL]")) {
                final String sUrl = args[1];
                try {
                    final URL url = new URL(sUrl);
                    loginBuilder.host(url.getHost());
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException("Malformed URL: " + sUrl);
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        bootBuilder.automatic(loginBuilder.build()).bootstrap(SwingBaseLauncher.class).run();
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Launch failure", ex);
                        JOptionPane.showMessageDialog(null, "Error: " + ex, "Launch failed", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }
            });
        } catch (final Exception ex) {
            log.log(Level.SEVERE, "Failed command, caught: " + ex, ex);
            SwingUtilities.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, "Error: " + ex, "Launch failed", JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
                    });
        }
    }
}
