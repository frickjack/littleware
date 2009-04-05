/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import littleware.apps.misc.ThumbManager;
import littleware.asset.*;
import littleware.base.UUIDFactory;
import littleware.apps.swingclient.event.*;


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
public class JAssetLink extends JAssetLinkRenderer implements LittleTool {
	private final static Logger   olog_generic = Logger.getLogger ( JAssetLink.class.getName() );
    private static Clipboard      oclip_copy = null;
    
    static {
        try {
            oclip_copy = Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch ( SecurityException e ) {
            // this will happen when running as an untrusted Applet
            olog_generic.log ( Level.WARNING, "Failed to access system clipboard, caught: " + e +
                               ", creating local clipboard instead"
                               );
            oclip_copy = new Clipboard ( "Asset link" );
        }
    }
    private static final long serialVersionUID = -3485310186352136477L;

    private final SimpleLittleTool      otool_handler = new SimpleLittleTool ( this );


    // popup menu on Cntrl-Click on JAssetLink
    private final JPopupMenu           owpopup_menu = new JPopupMenu ();
    {
        JMenuItem  witem_copy = new JMenuItem ( "Copy" );
        witem_copy.setMnemonic ( KeyEvent.VK_C );
        witem_copy.setAccelerator(KeyStroke.getKeyStroke(
                                                         KeyEvent.VK_C, ActionEvent.ALT_MASK));        
        // Copy to clipboard
        witem_copy.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( null != getLink() ) {
                    Transferable transfer_string = new StringSelection ( UUIDFactory.makeCleanString( getLink() ) );
                    oclip_copy.setContents ( transfer_string, null );
                }
            }
        }
                                       );
        owpopup_menu.add ( witem_copy );

        JMenuItem  witem_copy_id = new JMenuItem ( "Copy Id" );
        witem_copy_id.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                Transferable transfer_string = new StringSelection ( getText () );
                oclip_copy.setContents ( transfer_string, null );
            }
        }
                                       );
        owpopup_menu.add ( witem_copy_id );
        
        JMenuItem  witem_goto = new JMenuItem ( "Open" );
        witem_goto.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireNavEvent ( NavRequestEvent.NavMode.GENERIC );
            }
        }
                                       );
       owpopup_menu.add ( witem_goto );
       
       JMenuItem  witem_goto_new = new JMenuItem ( "Open new window" );
       witem_goto_new.addActionListener ( new ActionListener () {
            @Override
           public void actionPerformed(ActionEvent e) {
               fireNavEvent ( NavRequestEvent.NavMode.NEW_WINDOW );
           }
       }
                                      );
       owpopup_menu.add ( witem_goto_new );
       
    }

	

    /**
     * Shared utility for different event handlers
     * fires a NavRequestEvent to registered LittleListerns
     * assuming a link is registered with this widget.
     *
     * @param n_mode mode to fire event with
     */
    private void fireNavEvent ( NavRequestEvent.NavMode n_mode ) {
        UUID u_destination = getLink();
        
        if ( null != u_destination ) { 
            olog_generic.log ( Level.FINE, "Firing NavRequestEvent with mode: " + n_mode );
            otool_handler.fireLittleEvent ( new NavRequestEvent ( this,
                                                                  u_destination,
                                                                  n_mode
                                                                  )
                                            );
        }        
    }
    
    
    /**
     * MouseListener that handles a few things.
     */
    private class LabelMouseListener implements MouseListener {
        private Color    ocolor_old = null;
        // keep track of whether this is a popup-event or not
        private boolean  ob_popup = false;
        
        /**
         * Get the destination asset references by the JLabel we are listening to
         */
        public UUID getDestination () { 
            return getLink();
        }
        
        /** NOOP - handle mouseReleased instead */
        @Override
        public void mouseClicked(MouseEvent e) {}

        

        /** Popup if appropriate */
        @Override
        public void mousePressed(MouseEvent e) {
            ob_popup = evaluatePopup(e);
        }
        
        /**
         * Popup or navigate as appropriate
         */
        @Override
        public void mouseReleased(MouseEvent event_mouse ) {
            if ( (! evaluatePopup( event_mouse ))
                 && (! ob_popup)
                 ) {
                NavRequestEvent.NavMode n_mode = NavRequestEvent.NavMode.GENERIC;
                
                if ( event_mouse.getClickCount () > 1 ) {
                    n_mode = NavRequestEvent.NavMode.NEW_WINDOW;
                }
                
                fireNavEvent ( n_mode );                
            }
            ob_popup = false;
        }
        
        /**
         * Shared popup evaluator - Windows and Mac differ
         * on mouse-press vs. mouse-release evaluation point.
         *
         * @return true if the popup is displayed, false if NOOP
         */
        private boolean evaluatePopup(MouseEvent event_x ) {
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
        public void mouseEntered ( MouseEvent event_mouse ) {
            if ( null != getLink() ) {
                JLabel  wlabel_event = (JLabel) event_mouse.getSource ();
                ocolor_old = wlabel_event.getForeground ();
                wlabel_event.setForeground ( Color.CYAN );
            }
        }
        
        /**
         * Reset the foreground
         */
        @Override
        public void mouseExited ( MouseEvent event_mouse ) {
            if ( null != getLink() ) {
                ((JLabel) event_mouse.getSource ()).setForeground ( ocolor_old );
            }
        }
     
    }
    
    private final IconLibrary  olib_icon;
    private final ThumbManager omgrThumb;
    
    /**
     * Setup the link with the library to render icons
     *
     * @param lib_icon source of icons
     */
    @Inject
    public JAssetLink ( IconLibrary lib_icon, ThumbManager mgrThumb,
            AssetModelLibrary libAsset, AssetSearchManager search
            ) {
        super( lib_icon, mgrThumb, libAsset, search );
        olib_icon = lib_icon;
        omgrThumb = mgrThumb;
        this.setForeground ( Color.BLUE );
        this.addMouseListener ( new LabelMouseListener () );
        this.setToolTipText ( "Click to navigate, Ctrl-Click for menu" );
    }
    
    
    /**
     * Implement the TableCellRenderer interface so
     * we can draw table cells with this guy
     *
     * @param x_value should be an Asset
     */
    @Override
    public Component getTableCellRendererComponent ( JTable table, Object x_value, boolean isSelected, boolean hasFocus, int row, int column ) {
        setLink( (Asset) x_value );

        if(isSelected) {
            setBackground( table.getSelectionBackground());
        } else {
            setBackground( table.getBackground() );
        }
        setFont( table.getFont () );
        setOpaque ( true );
        setToolTipText ( null );
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
                                                  JList   wlist_assets,
                                                  Object  x_value,            
                                                  int     i_index,            
                                                  boolean b_selected,      
                                                  boolean b_hasfocus)    
    {
        setLink( (Asset) x_value );
        if (b_selected) {
            setBackground(wlist_assets.getSelectionBackground());
            setForeground(wlist_assets.getSelectionForeground());
        }
        else {
            setBackground(wlist_assets.getBackground());
            setForeground(wlist_assets.getForeground());
        }
        setEnabled(wlist_assets.isEnabled());
        setFont(wlist_assets.getFont());
        setOpaque(true);
        return this;
    }
    
    private DefaultTreeCellRenderer  otreerender_default = null;
    
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
                                           boolean b_hasFocus)
    {
        if ( null == otreerender_default ) {
            otreerender_default = new DefaultTreeCellRenderer ();
        }
        otreerender_default.getTreeCellRendererComponent ( wtree_assets, "bla", b_selected,
                                                           b_expanded, b_leaf, i_row,
                                                           b_hasFocus
                                                           );
        if ( (null != x_value)
             && (x_value instanceof DefaultMutableTreeNode)
             ) { // add this check - necessary at bootstrap
            Asset a_link = (Asset) ((DefaultMutableTreeNode) x_value).getUserObject (); 
            if ( null != a_link ) {
                otreerender_default.setIcon ( olib_icon.lookupIcon ( a_link.getAssetType () ) );
                otreerender_default.setText ( a_link.getName () );
            } else {
                otreerender_default.setText ( "null" );
            }
        }
        return otreerender_default;
    }
    
    
    /**
     * Set the link to point at the given asset
     *
     * @param a_linkto asset to link to
     */
    @Override
    public void setLink ( Asset a_linkto ) {
        super.setLink( a_linkto );
        this.setForeground ( Color.BLUE );
    }
    
    
    @Override
    public void	addLittleListener( LittleListener listen_little ) {
		otool_handler.addLittleListener ( listen_little );
	}
	
	
    @Override
	public void     removeLittleListener( LittleListener listen_little ) {
		otool_handler.removeLittleListener ( listen_little );
	}
    
}


