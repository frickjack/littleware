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
import com.google.inject.Provider;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import java.util.UUID;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.apps.client.event.AssetModelEvent;
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
    private static final Logger olog_generic = Logger.getLogger ( AssetView.class.getName() );
    private static final long serialVersionUID = -5561680971560382683L;
    
    private final AssetViewFactory      ofactory_view;
    private final IconLibrary           olib_icon;
    private final AssetModelLibrary     olib_asset;
    private final AssetRetriever        om_retriever;
    private final AbstractAssetView     oview_support = new AbstractAssetView ( this ) {
        // NOOP - active view handles events from model
        @Override
        public void eventFromModel ( LittleEvent evt_model ) {
            if ( (evt_model instanceof AssetModelEvent)
                 && AssetModel.Operation.assetsLinkingFrom.toString().equals( evt_model.getOperation() )
                    ) {
                // Need to update children view
                updateChildrenWidgetAndValidate ();
            }
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
    private final DefaultListModel      omodel_history = new DefaultListModel ();
    private final static int            oi_history_size = 10;
    private final JAssetLinkList        owlist_children;
    private final DefaultListModel      omodel_children = new DefaultListModel ();
    
    /**
     * Listener just propogates NavRequestEvents from JAssetLink through
     * to this object&apos;s listeners.
     */
    protected LittleListener olisten_bridge = new LittleListener () {
        @Override
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
     * Maintain a ListModel history of the assets
     * the user visits
     * 
     * @return omodel_history that some other part of the UI might view
     */
    public ListModel  getHistoryModel () {
        return omodel_history;
    }
    
    /**
     * Constructor stashes some stuff to support the browser.
     * Add a controller after construction to resond to
     * littleware.apps.swingclient.apps.NavRequestEvents.
     */
    @Inject
    public JAssetBrowser ( AssetViewFactory  factory_view,
                           IconLibrary       lib_icon,
                           AssetModelLibrary lib_asset,
                           AssetRetriever    m_retriever,
                           Provider<JAssetLinkList> provideLinkListView
                           ) {
        //super( new GridBagLayout () );
        //this.setLayout( new BoxLayout( this, BoxLayout.X_AXIS));
        ofactory_view = factory_view;
        olib_icon = lib_icon;
        olib_asset = lib_asset;
        om_retriever = m_retriever;

        /*
        JAssetLinkList wlist_history = provideLinkListView.get();
        wlist_history.setHeader("History" );
        wlist_history.setModel(omodel_history);
        wlist_history.setRenderThumbnail(false);
        wlist_history.addLittleListener ( olisten_bridge );
        */

        owlist_children = provideLinkListView.get();
        owlist_children.setHeader( "-------- Children (linking From) --------" );
        owlist_children.setModel( omodel_children );
        owlist_children.addLittleListener ( olisten_bridge );
        //owlist_children.setRenderThumbnail(false);

        GridBagConstraints grid_control = new GridBagConstraints ();
        grid_control.anchor = GridBagConstraints.NORTHWEST;
        grid_control.fill = GridBagConstraints.BOTH;
        grid_control.gridheight = 6; //GridBagConstraints.REMAINDER;
        grid_control.gridwidth = 2;
        grid_control.gridx = 0;
        grid_control.gridy = 0;
        //grid_control.weightx = 0.2;
        //this.add ( wlist_history, grid_control );

        //grid_control.gridx += grid_control.gridwidth;
        grid_control.gridwidth = 6;
        //grid_control.weightx = 0.6;
        this.add ( owpanel_view, grid_control );

        grid_control.gridx += grid_control.gridwidth;
        grid_control.gridwidth = 3; //GridBagConstraints.REMAINDER;
        //grid_control.weightx = 0.5;
        this.add ( owlist_children
                  //new JScrollPane( owlist_children ),
                  , grid_control
                   );
        
        oview_support.addPropertyChangeListener ( new PropertyChangeListener () {
            @Override
            public void propertyChange ( PropertyChangeEvent evt_prop ) {
                if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                    // Model has changed under us
                    SwingUtilities.invokeLater ( 
                                                 new Runnable () {
                        @Override
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
    
    
    
    @Override
    public AssetModel getAssetModel () {
        return oview_support.getAssetModel ();
    }

    private boolean  ob_in_sync = false;
    
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
        if ( ob_in_sync ) {
            // avoid recursive updates
            return;
        }
        try {
            ob_in_sync = true;
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
            updateChildrenWidgetAndValidate();
        } finally {
            ob_in_sync = false;
        }
    }

    private boolean ob_in_update = false;

    /**
     * Internal utility updates view of the children linking form an asset -
     * runs asynchronously if not on the Swing dispatch thread
     * Danger of recursive updates since this call does lookup to
     * collect child data, puts that data in the cache, and the cache
     * throws events when those child assets come in.
     */
    private void updateChildrenWidgetAndValidate () { // update the children
        if ( ob_in_update ) {
            // avoid recursive updates
            return;
        }
        if ( ! SwingUtilities.isEventDispatchThread() ) {
            SwingUtilities.invokeLater( new Runnable () {
                @Override
                public void run () { updateChildrenWidgetAndValidate (); }
            });
            return;
        }
        try {
            ob_in_update = true;
            omodel_children.clear();
            try {
                Map<String, UUID> v_children = om_retriever.getAssetIdsFrom(oview_support.getAssetModel().getAsset().getObjectId(), null);
                olog_generic.log( Level.FINE, "Syncing child UI: " + v_children.size() + " children" );
                for (UUID u_value : v_children.values()) {
                    omodel_children.addElement(u_value);
                }
            } catch (Exception e) {
                olog_generic.log(Level.WARNING, "Retrieving children caught unexpected: " + e);
            }
            owlist_children.setHeader( "Children of " + oview_support.getAssetModel().getAsset().getName() );
            owlist_children.setPreferredSize(
                    new Dimension(
                        owlist_children.getPreferredSize().width,
                        owpanel_view.getPreferredSize().height
                        )
                    );
            Component w_root = JUtil.findRoot ( this );
            w_root.validate ();
            if ( w_root instanceof Window ) {
                olog_generic.log ( Level.FINE, "Repacking window" );
                ((Window) w_root).pack ();
            }
        } finally {
            ob_in_update = false;
        }
    }

    
    
    @Override
    public void setAssetModel ( AssetModel model_asset ) {
        oview_support.setAssetModel ( model_asset );
    }

	
    
    @Override
    public void	addLittleListener( LittleListener listen_little ) {
		oview_support.addLittleListener ( listen_little );
	}
	
	
    @Override
	public void     removeLittleListener( LittleListener listen_little ) {
		oview_support.removeLittleListener ( listen_little );
	}
	    
}


