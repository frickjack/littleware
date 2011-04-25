/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import com.google.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import javax.swing.*;

import littleware.asset.*;
import littleware.base.UUIDFactory;
import littleware.apps.swingclient.event.*;
import littleware.base.Maybe;
import littleware.base.event.LittleListener;
import littleware.base.event.LittleTool;
import littleware.base.event.helper.SimpleLittleTool;

/** 
 * Specialization of a JLabel for setting up a link to an Asset.
 * Displays the asset-name with color animation when mouse
 * passes over, an asset-type based icon, and
 * throws a NavRequestEvent with the asset ID when the user clicks on the name.
 * Also includes a popup menu with mnemonic set that allows copying
 * the displayed text to the system clipboard.
 * JAssetLink does not view an AssetModel, since the data viewed is
 * all readonly, and this is actually viewing a link - not an asset.
 */
public class JAssetLink extends JLabel implements LittleTool {

    private final static Logger log = Logger.getLogger(JAssetLink.class.getName());
    private static Clipboard clipboard = null;

    static {
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (SecurityException ex) {
            // this will happen when running as an untrusted Applet
            log.log(Level.WARNING, "Failed to access system clipboard, creating local clipboard instead", ex);
            clipboard = new Clipboard("Asset link");
        }
    }
    private static final long serialVersionUID = -3485310186352136477L;
    private final SimpleLittleTool otool_handler = new SimpleLittleTool(this);
    private UUID ou_asset_link = null;
    // popup menu on Cntrl-Click on JAssetLink
    private final JPopupMenu owpopup_menu = new JPopupMenu();

    {
        JMenuItem witem_copy = new JMenuItem("Copy");
        witem_copy.setMnemonic(KeyEvent.VK_C);
        witem_copy.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.ALT_MASK));
        // Copy to clipboard
        witem_copy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (null != getLink()) {
                    Transferable transfer_string = new StringSelection(UUIDFactory.makeCleanString(getLink()));
                    clipboard.setContents(transfer_string, null);
                }
            }
        });
        owpopup_menu.add(witem_copy);

        JMenuItem witem_copy_id = new JMenuItem("Copy Id");
        witem_copy_id.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Transferable transfer_string = new StringSelection(getText());
                clipboard.setContents(transfer_string, null);
            }
        });
        owpopup_menu.add(witem_copy_id);

        JMenuItem witem_goto = new JMenuItem("Open");
        witem_goto.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fireNavEvent(NavRequestEvent.NavMode.GENERIC);
            }
        });
        owpopup_menu.add(witem_goto);

        JMenuItem witem_goto_new = new JMenuItem("Open new window");
        witem_goto_new.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fireNavEvent(NavRequestEvent.NavMode.NEW_WINDOW);
            }
        });
        owpopup_menu.add(witem_goto_new);

    }
    private final JAssetLinkRenderer orender;
    private final AssetModelLibrary olibAsset;
    private final AssetSearchManager osearch;

    /**
     * Property tracks whether to render the thumbnail or not
     * when available
     */
    public boolean isRenderThumbnail() {
        return orender.isRenderThumbnail();
    }

    public void setRenderThumbnail(boolean bRenderThumb) {
        orender.setRenderThumbnail(bRenderThumb);
    }

    /**
     * Shared utility for different event handlers
     * fires a NavRequestEvent to registered LittleListerns
     * assuming a link is registered with this widget.
     *
     * @param n_mode mode to fire event with
     */
    private void fireNavEvent(NavRequestEvent.NavMode n_mode) {
        UUID u_destination = getLink();

        if (null != u_destination) {
            log.log(Level.FINE, "Firing NavRequestEvent with mode: " + n_mode);
            otool_handler.fireLittleEvent(new NavRequestEvent(this,
                    u_destination,
                    n_mode));
        }
    }

    /**
     * MouseListener that handles a few things.
     */
    private class LabelMouseListener implements MouseListener {

        private Color ocolor_old = null;
        // keep track of whether this is a popup-event or not
        private boolean ob_popup = false;

        /**
         * Get the destination asset references by the JLabel we are listening to
         */
        public UUID getDestination() {
            return getLink();
        }

        /** NOOP - handle mouseReleased instead */
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        /** Popup if appropriate */
        @Override
        public void mousePressed(MouseEvent e) {
            ob_popup = evaluatePopup(e);
        }

        /**
         * Popup or navigate as appropriate
         */
        @Override
        public void mouseReleased(MouseEvent event_mouse) {
            if ((!evaluatePopup(event_mouse))
                    && (!ob_popup)) {
                NavRequestEvent.NavMode n_mode = NavRequestEvent.NavMode.GENERIC;

                if (event_mouse.getClickCount() > 1) {
                    n_mode = NavRequestEvent.NavMode.NEW_WINDOW;
                }

                fireNavEvent(n_mode);
            }
            ob_popup = false;
        }

        /**
         * Shared popup evaluator - Windows and Mac differ
         * on mouse-press vs. mouse-release evaluation point.
         *
         * @return true if the popup is displayed, false if NOOP
         */
        private boolean evaluatePopup(MouseEvent event_x) {
            if (event_x.isPopupTrigger()) {
                // show the pop-up menu...
                owpopup_menu.show(event_x.getComponent(),
                        event_x.getX(), event_x.getY());
                return true;
            }
            return false;
        }

        /**
         * Set the JLAbel foreground CYAN
         */
        @Override
        public void mouseEntered(MouseEvent event_mouse) {
            if (null != getLink()) {
                JLabel wlabel_event = (JLabel) event_mouse.getSource();
                ocolor_old = wlabel_event.getForeground();
                wlabel_event.setForeground(Color.CYAN);
            }
        }

        /**
         * Reset the foreground
         */
        @Override
        public void mouseExited(MouseEvent event_mouse) {
            if (null != getLink()) {
                ((JLabel) event_mouse.getSource()).setForeground(ocolor_old);
            }
        }
    }

    /**
     * Setup the link with the library to render icons
     *
     * @param lib_icon source of icons
     */
    @Inject
    public JAssetLink(
            AssetModelLibrary libAsset, AssetSearchManager search,
            JAssetLinkRenderer render) {
        this.setForeground(Color.BLUE);
        this.addMouseListener(new LabelMouseListener());
        this.setToolTipText("Click to navigate, Ctrl-Click for menu");
        orender = render;
        olibAsset = libAsset;
        osearch = search;
    }

    /**
     * Set the link to point at the given asset
     *
     * @param a_linkto asset to link to
     */
    public void setLink(Asset a_linkto) {
        if (null != a_linkto) {
            ou_asset_link = a_linkto.getId();
        } else {
            ou_asset_link = null;
        }
        orender.configureLabel(a_linkto, this);
        this.setForeground(Color.BLUE);
    }

    @Override
    public void addLittleListener(LittleListener listen_little) {
        otool_handler.addLittleListener(listen_little);
    }

    @Override
    public void removeLittleListener(LittleListener listen_little) {
        otool_handler.removeLittleListener(listen_little);
    }

    /**
     * Set the link info - retrieve the asset with u_id,
     * register it with the model-library, and
     * setup the display.  Eats checked exceptions, and just
     * sets up the display with bomb/error info.
     *
     * @param u_id to display - may be null - ignores other args if null
     * @param jLabelRender label subtype to display info to
     */
    public final void setLink(UUID u_id) {
        if (null == u_id) {
            setLink((Asset) null);
            return;
        }
        try {
            final Maybe<AssetModel> maybeModel = olibAsset.retrieveAssetModel(u_id, osearch);
            if (!maybeModel.isSet()) {
                setLink((Asset) null);
            } else {
                setLink(maybeModel.get().getAsset());
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to load object id " + u_id, ex);
            setLink((Asset) null);
        }
    }

    /**
     * Get the id of the asset the link points to - may be null,
     * may be an id of a "bomb" asset the user cannot access/load.
     */
    public final UUID getLink() {
        return ou_asset_link;
    }
}
