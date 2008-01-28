package littleware.apps.swingclient;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
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
public class JAssetLink extends JLabel implements LittleTool, ListCellRenderer, TableCellRenderer, TreeCellRenderer {
	private final static Logger   olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JAssetLink" );
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

	private UUID                        ou_asset_link = null;
    private final SimpleLittleTool      otool_handler = new SimpleLittleTool ( this );
    private final IconLibrary           olib_icon;

    // popup menu on Cntrl-Click on JAssetLink
    private final JPopupMenu           owpopup_menu = new JPopupMenu ();
    {
        JMenuItem  witem_copy = new JMenuItem ( "Copy" );
        witem_copy.setMnemonic ( KeyEvent.VK_C );
        witem_copy.setAccelerator(KeyStroke.getKeyStroke(
                                                         KeyEvent.VK_C, ActionEvent.ALT_MASK));        
        // Copy to clipboard
        witem_copy.addActionListener ( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                if ( null != ou_asset_link ) {
                    Transferable transfer_string = new StringSelection ( UUIDFactory.makeCleanString( ou_asset_link ) );
                    oclip_copy.setContents ( transfer_string, null );
                }
            }
        }
                                       );
        owpopup_menu.add ( witem_copy );

        JMenuItem  witem_copy_id = new JMenuItem ( "Copy Id" );
        witem_copy_id.addActionListener ( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                Transferable transfer_string = new StringSelection ( getText () );
                oclip_copy.setContents ( transfer_string, null );
            }
        }
                                       );
        owpopup_menu.add ( witem_copy_id );
        
        JMenuItem  witem_goto = new JMenuItem ( "Open" );
        witem_goto.addActionListener ( new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                fireNavEvent ( NavRequestEvent.NavMode.GENERIC );
            }
        }
                                       );
       owpopup_menu.add ( witem_goto );
       
       JMenuItem  witem_goto_new = new JMenuItem ( "Open new window" );
       witem_goto_new.addActionListener ( new ActionListener () {
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
        UUID u_destination = ou_asset_link;
        
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
            return ou_asset_link;
        }
        
        /** NOOP - handle mouseReleased instead */
        public void mouseClicked(MouseEvent e) {}

        

        /** Popup if appropriate */
        public void mousePressed(MouseEvent e) {
            ob_popup = evaluatePopup(e);
        }
        
        /**
         * Popup or navigate as appropriate
         */
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
        public void mouseEntered ( MouseEvent event_mouse ) {
            if ( null != ou_asset_link ) {
                JLabel  wlabel_event = (JLabel) event_mouse.getSource ();
                ocolor_old = wlabel_event.getForeground ();
                wlabel_event.setForeground ( Color.CYAN );
            }
        }
        
        /**
         * Reset the foreground
         */
        public void mouseExited ( MouseEvent event_mouse ) {
            if ( null != ou_asset_link ) {
                ((JLabel) event_mouse.getSource ()).setForeground ( ocolor_old );
            }
        }
     
    }
    
    
    
    /**
     * Setup the link with the library to render icons
     *
     * @param lib_icon source of icons
     */
    public JAssetLink ( IconLibrary lib_icon ) {
        super( "uninitialized",  SwingConstants.LEFT );
        this.setForeground ( Color.BLUE );
        olib_icon = lib_icon;
        this.addMouseListener ( new LabelMouseListener () );
        this.setToolTipText ( "Click to navigate, Ctrl-Click for menu" );
        setLink ( null, null, null );
    }
    
    /**
     * Setup a JAssetLink as a List/TableCellRenderer rather than
     * as a component to be displayed independently
     *
     * @param lib_icon to get icons from
     * @param b_renderer set true if this object is just 
     *                             acting as a cell renderer
     */
    public JAssetLink ( IconLibrary lib_icon, boolean b_renderer ) {
        super( "uninitialized",  SwingConstants.LEFT );
        olib_icon = lib_icon;
        setLink( null, null, null );
    }
    
    /**
     * Implement the TableCellRenderer interface so
     * we can draw table cells with this guy
     *
     * @param x_value should be an Asset
     */
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
     * Set the link info - retrieve the asset with u_id,
     * register it with the model-library, and
     * setup the display.  Eats checked exceptions, and just
     * sets up the display with bomb/error info.
     *
     * @param u_id to display - may be null - ignores other args if null
     * @param lib_asset to retrieve asset through
     * @param m_retriever to use with lib_asset.retrieveAssetModel
     */
    public void setLink ( UUID u_id, AssetModelLibrary lib_asset, 
                          AssetRetriever m_retriever
                          ) {
        this.setIcon ( null );
        
        ou_asset_link = u_id;
        if ( null == u_id ) {
            this.setForeground ( null );
            this.setText ( "<html><i>null</i></html>" );
        } else {
            String s_name = u_id.toString ();
            this.setForeground ( Color.BLUE );
            this.setText ( s_name );
            try {
                Asset a_linkto = lib_asset.retrieveAssetModel ( u_id, m_retriever ).getAsset ();
                setLink ( a_linkto );
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( GeneralSecurityException e ) {
                olog_generic.log ( Level.FINE, "Eating GeneralSecurityException: " + e + ", " +
                                   BaseException.getStackTrace ( e )
                                   );
                this.setIcon ( olib_icon.lookupIcon ( "littleware.bomb" ) );
            } catch ( Exception e ) {
                // set a tool tip later
                this.setIcon ( olib_icon.lookupIcon ( "littleware.screw" ) );
                olog_generic.log ( Level.WARNING, "Failed to retrieve asset info for " + u_id + ", caught: " + e );
            }
        }        
        
    }
    
    /**
     * Set the link to point at the given asset
     *
     * @param a_linkto asset to link to
     */
    public void setLink ( Asset a_linkto ) {
        ou_asset_link = a_linkto.getObjectId ();
        this.setForeground ( Color.BLUE );
        this.setIcon ( olib_icon.lookupIcon ( a_linkto.getAssetType () ) );
        this.setText ( a_linkto.getName () );
    }
    
    /**
     * Get the id of the asset the link points to - may be null,
     * may be an id of a "bomb" asset the user cannot access/load.
     */
    public UUID getLink () {
        return ou_asset_link;
    }
    
    public void	addLittleListener( LittleListener listen_little ) {
		otool_handler.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		otool_handler.removeLittleListener ( listen_little );
	}
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

