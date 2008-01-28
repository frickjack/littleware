package littleware.web.applet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.net.URL;

import littleware.base.*;
import littleware.base.swing.*;
import littleware.security.auth.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.SimpleAssetViewController;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.Asset;

/**
 * Applet based JAssetBrowser.
 * Expects the following applet parameters: <br />
 *     session_uuid: id of the user session to connect to <br />
 *     asset_uuid: id of asset to start browsing at, default to session_uuid if not set <br />
 */
public class Browser extends Toolbox {

    private JAssetBrowser owbrowser_asset = null;
    private JSimpleAssetToolbar owtoolbar_asset = null;

    /**
     * Allow subtypes to add listeners to and customize the browser
     */
    protected JAssetBrowser getBrowser() {
        return owbrowser_asset;
    }

    /**
     * Allow subtypes to add listeners to and customize the toolbar
     */
    protected JSimpleAssetToolbar getToolbar() {
        return owtoolbar_asset;
    }

    /**
     * Run on the Swing dispatch thread
     */
    private void buildUI() {
        String s_uuid_session = getParameter("session_uuid");
        String s_uuid_asset = getParameter("asset_uuid");

        if (null == s_uuid_asset) {
            s_uuid_asset = s_uuid_session;
        }
        if (null == s_uuid_session) {
            throw new AssertionFailedException("required session_uuid parameter not set");
        }

        try {
            UUID u_asset = UUIDFactory.parseUUID(s_uuid_asset);
            SessionHelper m_helper = getSessionHelper();
            AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);
            IconLibrary lib_icon = getIconLibrary();
            AssetModelLibrary lib_asset = getAssetModelLibrary();
            AssetViewFactory factory_view = getViewFactory();

            owbrowser_asset = new JAssetBrowser(factory_view, lib_icon, lib_asset, m_search);
            LittleListener listen_control = new SimpleAssetViewController((AssetView) owbrowser_asset,
                    m_search,
                    lib_asset);
            ((AssetView) owbrowser_asset).addLittleListener(listen_control);
            owtoolbar_asset = new JSimpleAssetToolbar((AssetView) owbrowser_asset,
                    lib_asset,
                    lib_icon,
                    m_search,
                    "Browser Toolbar");
            ((LittleTool) owtoolbar_asset).addLittleListener(listen_control);

            owbrowser_asset.setAssetModel(lib_asset.retrieveAssetModel(u_asset, m_search));

            final JFrame w_frame = new JFrame("Asset Browser");
            w_frame.setLayout(new BorderLayout());
            w_frame.getContentPane().add(owtoolbar_asset, BorderLayout.PAGE_START);
            w_frame.getContentPane().add(owbrowser_asset, BorderLayout.CENTER);
            w_frame.pack();
            w_frame.setVisible(true);
            final JButton wbutton_unhide = new JButton("Open Asset Browser");
            wbutton_unhide.addActionListener(
                    new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            w_frame.setVisible(true);
                        }
                    });

            getContentPane().add(wbutton_unhide, BorderLayout.CENTER);
        } catch (Exception e) {
            getContentPane().add(new JLabel("<html><body><font color=\"red\">Failed to establish RMI session, caught: " +
                    XmlSpecial.encode(e.toString()) +
                    "</font></body></html>"),
                    BorderLayout.CENTER);
        }

    }

    /**
     * Buildup the browser widgets
     */
    public void init() {
        super.init();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    buildUI();
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to build UI", e);
        }
    }

    public void start() {
    }

    public void stop() {
    }

    public void destroy() {
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

