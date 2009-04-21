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
import javax.swing.table.DefaultTableCellRenderer;
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
public class JAssetLinkRenderer implements ListCellRenderer, TableCellRenderer, TreeCellRenderer {

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

    private final IconLibrary olib_icon;
    private final ThumbManager omgrThumb;
    private final AssetSearchManager osearch;
    private final AssetModelLibrary olibAsset;
    private final JLabel     ojLabel = new JLabel ("uninitialized", SwingConstants.LEFT);
    {
        ojLabel.setForeground(Color.BLUE);
    }
    private boolean obRenderThumb = true;

    private final DefaultTreeCellRenderer orenderTree = new DefaultTreeCellRenderer();
    private final DefaultListCellRenderer orenderList = new DefaultListCellRenderer();
    private final DefaultTableCellRenderer orenderTable = new DefaultTableCellRenderer();

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
        olib_icon = lib_icon;
        omgrThumb = mgrThumb;
        osearch = search;
        olibAsset = libAsset;
    }

    /**
     * Internal utility determines type of xValue, then
     * invokes configureLabel
     *
     * @param xValue
     */
    private void configureUnknown(Object xValue, JLabel jLabel) {
        if (null == xValue) {
            jLabel.setText("null");
            jLabel.setIcon(null);
        } else if (xValue instanceof UUID) {
            configureLabel((UUID) xValue, jLabel);
        } else if (xValue instanceof Asset) {
            configureLabel((Asset) xValue, jLabel);
        } else if (xValue instanceof String) {
            jLabel.setText((String) xValue);
        } else {
            olog_generic.log(Level.WARNING, "Object of unknown type: " + xValue);
            jLabel.setText("ERR - wrong type: " + xValue);
        }
    }

    /**
     * Implement the TableCellRenderer interface so
     * we can draw table cells with this guy
     *
     * @param x_value should be an Asset
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object x_value, boolean isSelected, boolean hasFocus, int row, int column) {
        orenderTable.getTableCellRendererComponent( table, x_value, isSelected, hasFocus, row, column );
        configureUnknown( x_value, orenderTable );
        return orenderTable;
        /*..
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setOpaque(true);
        setToolTipText(null);
        return this;
         */
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
        orenderList.getListCellRendererComponent( wlist_assets, x_value, i_index,
                b_selected, b_hasfocus
                );
        configureUnknown( x_value, orenderList );
        return orenderList;
        /*..
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
         */
    }


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
        orenderTree.getTreeCellRendererComponent(wtree_assets, "bla", b_selected,
                b_expanded, b_leaf, i_row,
                b_hasFocus);
        if ((null != x_value) && (x_value instanceof DefaultMutableTreeNode)) { // add this check - necessary at bootstrap
            configureUnknown( ((DefaultMutableTreeNode) x_value).getUserObject(), orenderTree );
        } else {
            configureUnknown( x_value, orenderTree );
        }
        return orenderTree;
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


    public void configureLabel( Asset a_linkto, JLabel jLabelRender ) {
        if (null == a_linkto) {
            jLabelRender.setForeground(null);
            jLabelRender.setIcon( null );
            jLabelRender.setText("<html><i>null</i></html>");
            return;
        }
        
        Icon iconType = olib_icon.lookupIcon(a_linkto);
        UUID uThumb = a_linkto.getObjectId();

        if ( a_linkto.getAssetType().isA( AssetType.LINK )
              && (null != a_linkto.getToId() )
             ) {
            try {
                // Try to incorporate the "to" asset info
                final Icon iconBase = iconType;
                final Icon iconTo = olib_icon.lookupIcon( a_linkto.getToAsset(osearch));

                iconType = new Icon() {
                    @Override
                    public int getIconWidth () {
                        return iconBase.getIconWidth() + iconTo.getIconHeight();
                    }

                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        iconBase.paintIcon(c, g, x, y);
                        iconTo.paintIcon(c, g, x + iconBase.getIconWidth(), y );
                    }

                    @Override
                    public int getIconHeight() {
                        if ( iconBase.getIconHeight() > iconTo.getIconHeight () ) {
                            return iconBase.getIconHeight();
                        } else {
                            return iconTo.getIconHeight();
                        }
                    }
                };
                uThumb = a_linkto.getToId();
            } catch ( Exception ex ) {
                olog_generic.log( Level.WARNING, "Dangling to asset - failed to retrieve " + a_linkto.getObjectId() );
            }
        }
        
        if (obRenderThumb) {
            try {
                Thumb thumb = omgrThumb.loadThumb( uThumb );
                if (!thumb.isFallback()) {
                    final Icon iconBase = iconType;
                    final ImageIcon iconThumb = new ImageIcon(thumb.getThumb()) {

                        @Override
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                            super.paintIcon(c, g, x, y);
                            iconBase.paintIcon(c, g, x, y);
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

}


