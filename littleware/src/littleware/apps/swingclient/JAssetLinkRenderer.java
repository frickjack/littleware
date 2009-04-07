/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import com.google.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.*;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import littleware.apps.misc.ThumbManager;
import littleware.apps.misc.ThumbManager.Thumb;
import littleware.asset.*;
import littleware.base.BaseException;

/**
 * Specialization of a JLabel for rendering a link to an Asset.
 * Implements ListCellRenderer, TableCellRenderer, and TreeCellRenderer.
 * Displays the asset-name, thumbnail, and an asset-type based icon.
 */
public class JAssetLinkRenderer extends JLabel implements ListCellRenderer, TableCellRenderer, TreeCellRenderer {

    private final static Logger olog_generic = Logger.getLogger(JAssetLinkRenderer.class.getName());
    private static Clipboard oclip_copy = null;


    static {
        try {
            oclip_copy = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (SecurityException e) {
            // this will happen when running as an untrusted Applet
            olog_generic.log(Level.WARNING, "Failed to access system clipboard, caught: " + e +
                    ", creating local clipboard instead");
            oclip_copy = new Clipboard("Asset link");
        }
    }
    private static final long serialVersionUID = 4817821870438116270L;
    private UUID ou_asset_link = null;
    private final IconLibrary olib_icon;
    private final ThumbManager omgrThumb;
    private final AssetSearchManager osearch;
    private final AssetModelLibrary olibAsset;
    private boolean obRenderThumb = true;

    /**
     * Property tracks whether to render the thumbnail or not
     * when available
     */
    public boolean isRenderThumbnail() {
        return obRenderThumb;
    }

    public void setRenderThumbnail(boolean bRenderThumb) {
        obRenderThumb = bRenderThumb;
    }

    /**
     * Setup the link with the library to render icons
     *
     * @param lib_icon source of icons
     */
    @Inject
    public JAssetLinkRenderer(IconLibrary lib_icon, ThumbManager mgrThumb,
            AssetModelLibrary libAsset, AssetSearchManager search) {
        super("uninitialized", SwingConstants.LEFT);
        this.setForeground(Color.BLUE);
        olib_icon = lib_icon;
        omgrThumb = mgrThumb;
        osearch = search;
        olibAsset = libAsset;
        setLink((UUID) null);
    }

    /**
     * Implement the TableCellRenderer interface so
     * we can draw table cells with this guy
     *
     * @param x_value should be an Asset
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object x_value, boolean isSelected, boolean hasFocus, int row, int column) {
        setLink((Asset) x_value);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setOpaque(true);
        setToolTipText(null);
        return this;
    }

    /**
     * This is the only method defined by ListCellRenderer.
     * We just reconfigure the JLabel each time we're called.
     *
     * @param x_value should be an Asset
     */
    @Override
    public Component getListCellRendererComponent(
            JList wlist_assets,
            Object x_value,
            int i_index,
            boolean b_selected,
            boolean b_hasfocus) {
        if (x_value instanceof UUID) {
            setLink((UUID) x_value);
        } else if (x_value instanceof Asset) {
            setLink((Asset) x_value);
        } else if (null == x_value) {
            setLink((UUID) null);
        } else {
            olog_generic.log(Level.WARNING, "Given object of unexpected type: " + x_value.getClass());
            setLink((UUID) null);
        }
        if (b_selected) {
            setBackground(wlist_assets.getSelectionBackground());
            setForeground(wlist_assets.getSelectionForeground());
        } else {
            setBackground(wlist_assets.getBackground());
            setForeground(wlist_assets.getForeground());
        }
        setEnabled(wlist_assets.isEnabled());
        setFont(wlist_assets.getFont());
        setOpaque(true);
        return this;
    }
    private DefaultTreeCellRenderer otreerender_default = null;

    /**
     * This is the only method defined by TableCellRenderer.
     * We just reconfigure the JLabel each time we're called.
     *
     * @param x_value should be a DefaultMutableTreeNode with Asset userObject.
     */
    @Override
    public Component getTreeCellRendererComponent(JTree wtree_assets,
            Object x_value,
            boolean b_selected,
            boolean b_expanded,
            boolean b_leaf,
            int i_row,
            boolean b_hasFocus) {
        if (null == otreerender_default) {
            otreerender_default = new DefaultTreeCellRenderer();
        }
        otreerender_default.getTreeCellRendererComponent(wtree_assets, "bla", b_selected,
                b_expanded, b_leaf, i_row,
                b_hasFocus);
        if ((null != x_value) && (x_value instanceof DefaultMutableTreeNode)) { // add this check - necessary at bootstrap
            Object x_link = ((DefaultMutableTreeNode) x_value).getUserObject();
            if (null == x_link) {
                otreerender_default.setText("null");
            } else if ( x_link instanceof UUID ) {
                configureLabel( (UUID) x_link, otreerender_default );
            } else if ( x_link instanceof Asset ) {
                configureLabel( (Asset) x_link, otreerender_default );
            } else if ( x_link instanceof String ) {
                otreerender_default.setText( (String) x_link );
            } else {
                olog_generic.log( Level.WARNING, "Object of unknown type: " + x_link );
                otreerender_default.setText( "ERR - wrong type: " + x_link );
            }
        }
        return otreerender_default;
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
    public final void setLink(UUID u_id ) {
        ou_asset_link = u_id;
        configureLabel( u_id, this );
    }

    private void configureLabel(UUID u_id, JLabel jLabelRender ) {
        jLabelRender.setIcon(null);
        
        if (null == u_id) {
            jLabelRender.setForeground(null);
            jLabelRender.setText("<html><i>null</i></html>");
            return;
        }
        String s_name = u_id.toString();
        jLabelRender.setForeground(Color.BLUE);
        jLabelRender.setText(s_name);
        try {
            Asset a_linkto = olibAsset.retrieveAssetModel(u_id, osearch).getAsset();
            configureLabel(a_linkto, jLabelRender );
        } catch (RuntimeException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            olog_generic.log(Level.FINE, "Eating GeneralSecurityException: " + e + ", " +
                    BaseException.getStackTrace(e));
            jLabelRender.setIcon(olib_icon.lookupIcon("littleware.bomb"));
        } catch (Exception e) {
            // set a tool tip later
            jLabelRender.setIcon(olib_icon.lookupIcon("littleware.screw"));
            olog_generic.log(Level.WARNING, "Failed to retrieve asset info for " + u_id + ", caught: " + e);
        }
    }

    /**
     * Set the link to point at the given asset
     *
     * @param a_linkto asset to link to
     */
    public void setLink(Asset a_linkto) {
        ou_asset_link = a_linkto.getObjectId();
        configureLabel( a_linkto, this );
    }

    private void configureLabel( Asset a_linkto, JLabel jLabelRender ) {
        if (null == a_linkto) {
            jLabelRender.setForeground(null);
            jLabelRender.setText("<html><i>null</i></html>");
            return;
        }
        
        final Icon iconType = olib_icon.lookupIcon(a_linkto.getAssetType());
        
        if (obRenderThumb) {
            try {
                Thumb thumb = omgrThumb.loadThumb(a_linkto.getObjectId());
                if (!thumb.isFallback()) {
                    final ImageIcon iconThumb = new ImageIcon(thumb.getThumb()) {

                        @Override
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                            super.paintIcon(c, g, x, y);
                            if ( x < iconType.getIconWidth()
                                    && y < iconType.getIconHeight()
                                    ) {
                                iconType.paintIcon(c, g, x, y);
                            }
                        }
                    };
                    jLabelRender.setIcon(iconThumb);
                } else {
                    jLabelRender.setIcon(iconType);
                }
            } catch (Exception ex) {
                jLabelRender.setIcon(iconType);
                olog_generic.log(Level.WARNING, "Failed to load thumbnail for asset " + a_linkto.getObjectId());
            }
        } else {
            jLabelRender.setIcon(iconType);
        }

        jLabelRender.setText(a_linkto.getName());
    }

    /**
     * Get the id of the asset the link points to - may be null,
     * may be an id of a "bomb" asset the user cannot access/load.
     */
    public final UUID getLink() {
        return ou_asset_link;
    }
}


