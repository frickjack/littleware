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
import littleware.asset.*;

/**
 * Applet based AssetEditor launcher.
 * Expects the following applet parameters: <br />
 *     session_uuid: id of the user session to connect to <br />
 *     asset_uuid: id of asset to start browsing at <br />
 */
public class Editor extends JApplet {

    /**
     * Internal utility to build the UI on the Swing dispatch thread
     */
    public void buildUI() {
        String s_uuid_session = getParameter("session_uuid");
        String s_uuid_asset = getParameter("asset_uuid");

        if (null == s_uuid_asset) {
            s_uuid_asset = s_uuid_session;
        }
        if (null == s_uuid_session) {
            throw new AssertionFailedException("required session_uuid parameter not set");
        }

        try {
            URL url_codebase = getCodeBase();
            SessionManager m_session = SessionUtil.getSessionManager(url_codebase.getHost(), SessionUtil.getRegistryPort());
            UUID u_session = UUIDFactory.parseUUID(s_uuid_session);
            UUID u_asset = UUIDFactory.parseUUID(s_uuid_asset);
            SessionHelper m_helper = m_session.getSessionHelper(u_session);
            AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);
            AssetManager m_asset = m_helper.getService(ServiceType.ASSET_MANAGER);
            IconLibrary lib_icon = new WebIconLibrary(url_codebase.getHost() + "/littleware/lib/icons");
            AssetModelLibrary lib_asset = new SimpleAssetModelLibrary();
            AssetViewFactory factory_view = new SimpleAssetViewFactory(m_search, lib_icon);
            AssetViewFactory factory_editor = new EditorAssetViewFactory(m_asset, m_search, lib_icon, factory_view);
            AssetEditor w_editor = (AssetEditor) factory_editor.createView ( lib_asset.retrieveAssetModel ( u_asset, m_search ) );

            final JFrame w_frame = new JFrame("Asset Editor");
            w_frame.setLayout(new BorderLayout());
            w_frame.getContentPane().add((JComponent) w_editor, BorderLayout.CENTER);
            w_frame.pack();
            //w_frame.setVisible( true );
            final JButton wbutton_unhide = new JButton("Open Editor");
            wbutton_unhide.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    w_frame.setVisible(true);
                }
            });

            getContentPane().add(wbutton_unhide, BorderLayout.CENTER);
        } catch (Exception e) {
            getContentPane().add(new JLabel("<html><body><font color=\"red\">Failed to establish RMI session, caught: " + XmlSpecial.encode(e.toString()) + "</font></body></html>"), BorderLayout.CENTER);
        }
    }

    /**
     * Buildup the editor widgets
     */
    public void init() {
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