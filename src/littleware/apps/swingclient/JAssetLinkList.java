package littleware.apps.swingclient;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.base.swing.JUtil;
import littleware.apps.swingclient.event.*;


/** 
 * Component that maintains a list of JAssetLinkList.
 */
public class JAssetLinkList extends JPanel implements LittleTool, ListDataListener {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JAssetLinkList" );
	
	private UUID                        ou_asset_link = null;
    private final SimpleLittleTool      otool_handler = new SimpleLittleTool ( this );
    private final IconLibrary           olib_icon;
    private final ListModel             omodel_list;
    private final AssetModelLibrary     olib_asset;
    private final AssetRetriever        om_retriever;
    private final GridBagConstraints    ogrid_control = new GridBagConstraints ();
    private final java.util.List<JAssetLink>     ov_memberlinks = new ArrayList<JAssetLink> ();

    
    {
        ogrid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        ogrid_control.fill = GridBagConstraints.HORIZONTAL;
        ogrid_control.gridx = 0;
        ogrid_control.gridy = GridBagConstraints.RELATIVE;
        ogrid_control.insets = new Insets ( 2,2,2,2 );        
    }
    
    /**
     * Forward events from the component JAssetLinks to the listeners
     * registered with this widget.
     */
    private LittleListener olisten_bridge = new LittleListener () {
        public void receiveLittleEvent ( LittleEvent event_little ) {
            if ( event_little instanceof NavRequestEvent ) {
                //event_little.setSource ( JGenericAssetView.this );
                NavRequestEvent event_nav = (NavRequestEvent) event_little;
                otool_handler.fireLittleEvent ( new NavRequestEvent ( JAssetLinkList.this,
                                                        event_nav.getDestination (),
                                                        event_nav.getNavMode ()
                                                        )
                                  );
            }
        }
    };
    

    /**
     * Little utility method that by default just calls: <br />
     *     wlink_asset.setLink ( a_view );  <br />
     * , but that a subtype may override to customize for
     * things like an AclEntry where we want to display the
     * entry Principal and list of permissions rather than the
     * AclEntry asset itself.
     *
     * @param wlink_asset widget to setup
     * @param a_view to associate with the wlink_asset widget
     */
    protected void setLink ( JAssetLink wlink_asset, Asset a_view ) {
        wlink_asset.setLink ( a_view );
    }
    
    /**
     * Setup the link with some handlers to retrieve data,
     * add this as a ListModel listener,
     * and initialize the UI to the initial state of the supplied ListModel.
     *
     * @param model_list list of assets to observe - may contain Asset
     *                      and/or UUID instances
     * @param lib_icon source of icons
     * @param lib_asset asset-model library to sync against when given a UUID to retrieve
     * @param m_retriever to resolve asset UUIDs with 
     * @param s_header to put at the top of the list 
     */
    public JAssetLinkList ( 
                            ListModel model_list, 
                            IconLibrary lib_icon, 
                            AssetModelLibrary lib_asset,
                            AssetRetriever m_retriever,
                            String s_header
                            ) {
        super( new GridBagLayout () );
        olib_icon = lib_icon;
        omodel_list = model_list;
        om_retriever = m_retriever;
        olib_asset = lib_asset;
        this.add ( new JLabel( "<html><b>" + s_header + "</b></html>" ), ogrid_control );
        omodel_list.addListDataListener ( this );
    }
    
    /**
     * Sync the UI list view with the contents of the 
     * internal list model.
     */
    private void syncListUI () {
        java.util.List<Asset>        v_members = new ArrayList<Asset> ();

        final int i_widgets = ov_memberlinks.size ();
        final int i_size = omodel_list.getSize ();
        //olog_generic.log ( Level.FINE, "Syncing UI, list size: " + i_size );
        for ( int i_count=0; i_count < i_size; ++i_count ) {
            if ( i_count >= i_widgets ) {
                JAssetLink  wlink_new = new JAssetLink( olib_icon );
                
                wlink_new.addLittleListener ( olisten_bridge );
                ov_memberlinks.add ( wlink_new );
                this.add ( wlink_new, ogrid_control );
            }
            
            JAssetLink wlink_member = ov_memberlinks.get ( i_count );
            Object x_member = omodel_list.getElementAt ( i_count );
            // List of Assets and UUIDs
            if ( x_member instanceof Asset ) {
                v_members.add ( (Asset) x_member );
                this.setLink ( wlink_member, (Asset) x_member );
            } else {
                try {
                    // Try to retrieve the asset and invoke setLink(), which may be customized 
                    this.setLink ( wlink_member, 
                                   olib_asset.retrieveAssetModel ( (UUID) x_member, om_retriever ).getAsset () 
                                   );
                } catch ( RuntimeException e ) {
                    throw e;
                } catch ( Exception e ) {
                    wlink_member.setLink ( (UUID) x_member, olib_asset, om_retriever );
                }
            }
        }
        
        // Cleanup extra widgets
        for ( int i=i_widgets; i > i_size; --i ) {
            JAssetLink wlink_remove = ov_memberlinks.remove ( i - 1 );
            this.remove ( wlink_remove );
        } 
        
        if ( i_size != i_widgets ) {
            // repack when layout changes
            Component w_root = JUtil.findRoot ( this );
            w_root.validate ();
            
            if ( w_root instanceof Window ) {
                ((Window) w_root).pack ();
            }
            
        }
    }

    /**
     * Get the model this view is watching
     */
    public ListModel getAssetModel () {
        return omodel_list;
    }
    
    public void	addLittleListener( LittleListener listen_little ) {
		otool_handler.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		otool_handler.removeLittleListener ( listen_little );
	}
    
    public void contentsChanged ( ListDataEvent event_list ) {
        syncListUI ();
    }
    
    public void intervalAdded ( ListDataEvent event_list ) {
        syncListUI ();
    }
    
    public void intervalRemoved( ListDataEvent event_list ) {
        syncListUI ();
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

