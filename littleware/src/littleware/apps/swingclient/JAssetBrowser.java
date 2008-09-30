package littleware.apps.swingclient;

import com.google.inject.Inject;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.swing.JUtil;
import littleware.apps.swingclient.event.*;

/**
 * Asset viewer/navigater.
 * Extends passive AssetView with controls to
 * navigate the view to different assets.
 * Propogates NavRequestEvents to listeners
 * when the browser navigates to view a different model.
 */
public class JAssetBrowser extends JPanel implements AssetView {
    private static Logger olog_generic = Logger.getLogger ( "littleware.apps.swingclient.AssetView" );
    
    private final AssetViewFactory      ofactory_view;
    private final IconLibrary           olib_icon;
    private final AssetModelLibrary     olib_asset;
    private final AssetRetriever        om_retriever;
    private final AbstractAssetView     oview_support = new AbstractAssetView ( this ) {
        // NOOP - active view handles events from model
        public void eventFromModel ( LittleEvent evt_model ) {
            
        }
    };

    
    private AssetView                   oview_current = null;
    private final GridBagConstraints    ogrid_control = new GridBagConstraints ();
    {
        ogrid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        ogrid_control.fill = GridBagConstraints.BOTH;
        ogrid_control.gridx = 0;
        ogrid_control.gridy = 0;
    }
    private final JPanel                owpanel_view = new JPanel ( new GridBagLayout () );
    private final JAssetLinkList        owlist_history;
    private final DefaultListModel      omodel_history = new DefaultListModel ();
    private final static int            oi_history_size = 10;
    private final JAssetLinkList        owlist_children;
    private final DefaultListModel      omodel_children = new DefaultListModel ();
    
    /**
     * Listener just propogates NavRequestEvents from JAssetLink through
     * to this object&apos;s listeners.
     */
    protected LittleListener olisten_bridge = new LittleListener () {
        public void receiveLittleEvent ( LittleEvent event_little ) {
            if ( event_little instanceof NavRequestEvent ) {
                //event_little.setSource ( JGenericAssetView.this );
                NavRequestEvent event_nav = (NavRequestEvent) event_little;
                oview_support.fireLittleEvent ( new NavRequestEvent ( JAssetBrowser.this,
                                                        event_nav.getDestination (),
                                                        event_nav.getNavMode ()
                                                        )
                                  );
            }
        }
    };
    
    
    /**
     * Constructor stashes some stuff to support the browser.
     * Add a controller after construction to resond to
     * littleware.apps.swingclient.apps.NavRequestEvents.
     */
    @Inject
    public JAssetBrowser ( AssetViewFactory  factory_view,
                           IconLibrary       lib_icon,
                           AssetModelLibrary lib_asset,
                           AssetRetriever    m_retriever
                           ) {
        super( new GridBagLayout () );
        ofactory_view = factory_view;
        olib_icon = lib_icon;
        olib_asset = lib_asset;
        om_retriever = m_retriever; 
        owlist_history = new JAssetLinkList ( omodel_history, lib_icon, lib_asset, m_retriever, "History" );
        owlist_history.addLittleListener ( olisten_bridge );
        owlist_children = new JAssetLinkList ( omodel_children, lib_icon, lib_asset, m_retriever, "Children (linking From)" );
        owlist_children.addLittleListener ( olisten_bridge );

        GridBagConstraints grid_control = new GridBagConstraints ();
        grid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        grid_control.fill = GridBagConstraints.BOTH;
        grid_control.gridheight = 4;
        grid_control.gridx = 0;
        grid_control.gridy = 0;
        //grid_control.weightx = 0.25;
        this.add ( owlist_history, grid_control );
        grid_control.gridy += grid_control.gridheight;
        this.add ( new JScrollPane( owlist_children,  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ),
                   grid_control
                   );
        grid_control.gridx = 2;
        grid_control.gridy = 0;
        grid_control.gridheight = GridBagConstraints.REMAINDER;
        grid_control.gridwidth = GridBagConstraints.REMAINDER;
        grid_control.weightx = 0.8;
        this.add ( owpanel_view, grid_control );
        
        oview_support.addPropertyChangeListener ( new PropertyChangeListener () {
            public void propertyChange ( PropertyChangeEvent evt_prop ) {
                if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                    // Model has changed under us
                    SwingUtilities.invokeLater ( 
                                                 new Runnable () {
                                                     public void run () {
                                                         syncAssetUI ();
                                                     }
                                                 }
                                                 );                
                }
                // Propagate to listeners on this object
                firePropertyChange ( evt_prop.getPropertyName (),
                                     evt_prop.getOldValue (),
                                     evt_prop.getNewValue ()
                                     );
            }
        }
                                    );
        
    }
    
    
    
    public AssetModel getAssetModel () {
        return oview_support.getAssetModel ();
    }
    
    /**
     * Invoke when the underlying model has changed,
     * and we need to update the UI accordingly.
     */
    private void syncAssetUI () {
        if ( null == oview_support.getAssetModel () ) {
            // what the frick ?
            olog_generic.log ( Level.WARNING, "Assetbrowser has null AssetModel" );
            return;
        }
        // update the core view
        if ( (null != oview_current)
             && (null != oview_support.getAssetModel ())
             && ofactory_view.checkView ( oview_current, oview_support.getAssetModel () ) 
             ) {
            olog_generic.log ( Level.FINE, "Updating current view to new model: " + oview_support.getAssetModel ().getAsset () );
            oview_current.setAssetModel ( oview_support.getAssetModel () );
        } else {
            olog_generic.log ( Level.FINE, "Updating new view to new model: " + oview_support.getAssetModel ().getAsset () );
            // else
            AssetView view_new = ofactory_view.createView ( oview_support.getAssetModel () );
            if ( null != oview_current ) {
                oview_current.removeLittleListener ( olisten_bridge );
                owpanel_view.remove ( (JComponent) oview_current );
            }
            view_new.addLittleListener ( olisten_bridge );
            oview_current = view_new;
            owpanel_view.add ( (JComponent) view_new, ogrid_control ); //, 1
        }
        
        { // update the history
            omodel_history.addElement ( oview_support.getAssetModel ().getAsset () );
            if ( omodel_history.getSize () > oi_history_size ) {
                omodel_history.remove( 0 );
            }
        }
        { // update the children
            omodel_children.clear ();
            try {
                Map<String,UUID> v_children = om_retriever.getAssetIdsFrom ( oview_support.getAssetModel ().getAsset ().getObjectId (), null );
                for ( UUID u_value : v_children.values () ) {
                    omodel_children.addElement ( u_value );
                }
            } catch ( Exception e ) {
                olog_generic.log ( Level.WARNING, "Retrieving children caught unexpected: " + e );
            }
        }
        
        Component w_root = JUtil.findRoot ( this );
        w_root.validate ();
        if ( w_root instanceof Window ) {
            olog_generic.log ( Level.FINE, "Repacking window" );
            ((Window) w_root).pack ();
        }    
    }

    
    
    public void setAssetModel ( AssetModel model_asset ) {
        oview_support.setAssetModel ( model_asset );
    }

	
    
    public void	addLittleListener( LittleListener listen_little ) {
		oview_support.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		oview_support.removeLittleListener ( listen_little );
	}
	    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

