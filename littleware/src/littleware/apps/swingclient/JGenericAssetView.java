/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.swingclient;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.*;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.UUIDFactory;
import littleware.apps.swingclient.event.*;

/** 
 * Simple JPanel based view of a generic asset
 */
public class JGenericAssetView extends JPanel implements AssetView {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGenericAssetView" );
	
    private final AssetRetriever        om_retriever;
    private final AbstractAssetView     oview_util = new AbstractAssetView ( this ) {
        /** Events form the data model */
        public void eventFromModel ( LittleEvent evt_from_model ) {
            JGenericAssetView.this.eventFromModel ( evt_from_model );
        }
    };
    
    private final IconLibrary           olib_icon;
 
    /** Panel to stuff summary data into */
    private final JPanel     owpanel_summary = new JPanel( new GridBagLayout () );
    /** Panel to stuff info that can be toggled visible/invisible */    
    private final JPanel      owpanel_details = new JPanel ( new GridBagLayout () );
    // Setup a button to toggle the detals panel visible/invisible
    private final JButton     owbutton_details = new JButton ( "+" );
    {
        owbutton_details.setToolTipText ( "View/Hide Asset details" );
    }
        
    /**
     * Each JLabel holds a piece of data about the asset.
     * Some of the labels act as links to other assets.
     */
    private final JLabel      owlabel_type = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_name = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_id = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_value = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_date_created = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_date_updated = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_start = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_end = new JLabel ( "uninitialized", SwingConstants.LEFT );
    private final JLabel      owlabel_transaction = new JLabel ( "uninitialized", SwingConstants.LEFT );

    private final JAssetLink      owlink_acl;
    private final JAssetLink      owlink_to;
    private final JAssetLink      owlink_from;
    private final JAssetLink      owlink_owner;
    private final JAssetLink      owlink_home;
    private final JAssetLink      owlink_creator;
    private final JAssetLink      owlink_updater;
    
    private final JTextArea   owtext_comment = new JTextArea ( 2, 40 );
    private final JTextArea   owtext_update = new JTextArea ( 2, 40 );
    private final JTextArea   owtext_data = new JTextArea ( 5, 40 );
    
    private final JTabbedPane  owtab_stuff = new JTabbedPane ();    
    
    /**
     * Internal utility to configure a row in the UI that displays generic String data
     *
     * @param s_label to prefix the data with
     * @param wlabel_data label referencing an asset by name or id
     * @param wpanel_addto the panel to add to
     * @param grid_control IN/OUT paramter to reference and update -
     *             advances to column 0 of next row before adding widgets to this     
     */
    private void configureDataRow ( String s_label,
                                    JLabel wlabel_data,
                                      JPanel wpanel_addto,
                                    GridBagConstraints grid_control
                                    ) 
    { 
        grid_control.gridx = 0;
        grid_control.gridy += grid_control.gridheight;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 1;

        wpanel_addto.add ( new JLabel ( s_label, SwingConstants.RIGHT ),
                   grid_control
                   );
        grid_control.gridx += grid_control.gridwidth;
        wpanel_addto.add ( wlabel_data, grid_control );
    }
    
    /**
     * Internal utility to configure a row in the UI that displays generic 
     * String data within a text area.
     *
     * @param s_label to prefix the data with
     * @param wtext_data label referencing an asset by name or id
     * @param wpanel_addto the panel to add to
     * @param grid_control IN/OUT paramter to reference and update -
     *             advances to column 0 of next row before adding widgets to this     
     */
    private void configureDataRow ( String s_label,
                                    JTextArea wtext_data,
                                      JPanel wpanel_addto,
                                    GridBagConstraints grid_control
                                    ) 
    {         
        grid_control.gridx = 0;
        grid_control.gridy += grid_control.gridheight;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 1;
        grid_control.fill = GridBagConstraints.HORIZONTAL;
        wpanel_addto.add ( new JLabel ( s_label ), grid_control );
        grid_control.gridy += grid_control.gridheight;
        grid_control.gridwidth = 2;
        grid_control.gridheight = 4;
        grid_control.fill = GridBagConstraints.BOTH;
        grid_control.weightx = 0.7;
        grid_control.weighty = 0.2;
        wtext_data.setLineWrap ( true );
        wtext_data.setEditable ( false );
        wpanel_addto.add ( new JScrollPane( wtext_data,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                                    ),
                   grid_control 
                   );

        grid_control.fill = GridBagConstraints.HORIZONTAL;
        grid_control.weighty = 0.0;
        grid_control.weightx = 0.0;
    }
    
    
    /**
     * Listener just propogates NavRequestEvents from JAssetLink through
     * to this object&apos;s listeners.
     */
    protected LittleListener olisten_bridge = new LittleListener () {
        public void receiveLittleEvent ( LittleEvent event_little ) {
            if ( event_little instanceof NavRequestEvent ) {
                //event_little.setSource ( JGenericAssetView.this );
                NavRequestEvent event_nav = (NavRequestEvent) event_little;
                fireLittleEvent ( new NavRequestEvent ( JGenericAssetView.this,
                                                                         event_nav.getDestination (),
                                                                         event_nav.getNavMode ()
                                                                         )
                                                   );
            }
        }
    };

    
    /**
     * Internal utility to help configure a row of the UI with info referencing
     * an asset.  
     * 
     * @param s_label to prefix the data with
     * @param wlink_data label referencing an asset by name or id
     * @param wpanel_addto the panel to add to
     * @param grid_control IN/OUT paramter to reference and update -
     *             advances to column 0 of next row before adding widgets to wpanel_addto
     */
    private void configureAssetRow ( 
                                       String s_label,
                                       JAssetLink wlink_data,
                                       JPanel wpanel_addto,
                                       GridBagConstraints grid_control
                                       ) 
    {         
        grid_control.gridx = 0;
        grid_control.gridy += grid_control.gridheight;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 1;
        wpanel_addto.add ( new JLabel ( s_label, SwingConstants.RIGHT ),
                   grid_control
                   );
        grid_control.gridx += grid_control.gridwidth;
        wpanel_addto.add ( wlink_data, grid_control );
        wlink_data.addLittleListener (
                                     olisten_bridge
                                      );
    }

    //---------------------------------------
    
    
    
    
    
    /**
     * Build the UI.
     * Subtypes may override or extend.
     */
    protected void buildAssetUI () {
		this.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
		this.setLayout ( new GridBagLayout () );
                
		owpanel_details.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );

        GridBagConstraints grid_control = new GridBagConstraints();
        
        grid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        grid_control.gridwidth = 1;
        grid_control.gridheight = 1;
        grid_control.ipadx = 1;
        grid_control.ipady = 1;
        grid_control.insets = new Insets ( 2,2,2,2 );
        grid_control.fill = GridBagConstraints.HORIZONTAL;
        
        { // Asset type and icon
            //grid_control.gridwidth = GridBagConstraints.REMAINDER;
            grid_control.gridx = 0;
            grid_control.gridy = 0;
            this.add ( new JLabel ( "Asset type:", SwingConstants.RIGHT ), 
                       grid_control 
                       );
            grid_control.gridx += grid_control.gridwidth;
            this.add ( owlabel_type, grid_control );
            
            configureDataRow ( "Name:",
                               owlabel_name,
                               this,
                               grid_control
                               );
        } 
        {
            GridBagConstraints   gcontrol_summary = (GridBagConstraints) grid_control.clone ();
            gcontrol_summary.gridheight = 1;
            gcontrol_summary.gridwidth = 1;
            gcontrol_summary.gridx = 0;
            gcontrol_summary.gridy = 0;
            gcontrol_summary.fill = GridBagConstraints.HORIZONTAL;            

            configureAssetRow ( "Home:",
                                owlink_home,
                                owpanel_summary,
                                gcontrol_summary
                                );
            configureAssetRow ( "From:",
                                owlink_from,
                                owpanel_summary,
                                gcontrol_summary
                                );
            configureAssetRow ( "To:",
                                owlink_to,
                                owpanel_summary,
                                gcontrol_summary
                                );
            
                            
            configureAssetRow ( "Owner:",
                                owlink_owner,
                                owpanel_summary,
                                gcontrol_summary
                                );
            
            configureAssetRow ( "Acl:",
                                owlink_acl,
                                owpanel_summary,
                                gcontrol_summary
                                );
            configureDataRow ( "Comment:",
                               owtext_comment,
                               owpanel_summary,
                               gcontrol_summary
                               );
        }
        
        { // Fill in the details panel
            GridBagConstraints   gcontrol_details = (GridBagConstraints) grid_control.clone ();
            gcontrol_details.gridheight = 1;
            gcontrol_details.gridwidth = 1;
            gcontrol_details.gridx = 0;
            gcontrol_details.gridy = 0;
            gcontrol_details.fill = GridBagConstraints.HORIZONTAL;
            configureDataRow ( "Start:",
                               owlabel_start,
                               owpanel_details,
                               gcontrol_details
                               );
            
            configureDataRow ( "End:",
                               owlabel_end,
                               owpanel_details,
                               gcontrol_details
                               );
            configureDataRow ( "Transaction:",
                               owlabel_transaction,
                               owpanel_details,
                               gcontrol_details
                               );
            
            configureDataRow ( "Id:",
                               owlabel_id,
                               owpanel_details,
                               gcontrol_details
                               );
            
            configureDataRow ( "Value:",
                               owlabel_value,
                               owpanel_details,
                               gcontrol_details
                               );
            
            /*..
                configureDataRow ( "Comment:",
                                   owtext_comment,
                                   owpanel_details,
                                   gcontrol_details
                                   );
            ..*/
            configureAssetRow ( "Creator:",
                                owlink_creator,
                                owpanel_details,
                                gcontrol_details
                                );
            
            configureDataRow ( "Create date:",
                               owlabel_date_created,
                               owpanel_details,
                               gcontrol_details
                               );        
            
            configureAssetRow ( "Last updater:",
                                owlink_updater,
                                owpanel_details,
                                gcontrol_details
                                );
            
            configureDataRow ( "Last update date:",
                               owlabel_date_updated,
                               owpanel_details,
                               gcontrol_details
                               );
            
            configureDataRow ( "Last update comment:",
                               owtext_update,
                               owpanel_details,
                               gcontrol_details 
                               );
            
            configureDataRow ( "Data block:",
                               owtext_data,
                               owpanel_details,
                               gcontrol_details 
                               );
            
            gcontrol_details.gridy += gcontrol_details.gridheight;
            gcontrol_details.gridx = 0;
            gcontrol_details.gridwidth = 1;
            gcontrol_details.gridheight = 1;
        }
        grid_control.gridy += grid_control.gridheight;
        grid_control.gridx = 0;
        grid_control.gridheight = GridBagConstraints.REMAINDER;
        grid_control.gridwidth = 2;
        grid_control.fill = GridBagConstraints.BOTH;        
                
        owtab_stuff.add ( "Summary", owpanel_summary );
        owtab_stuff.add ( "Details", owpanel_details );
        
        this.add (  owtab_stuff, grid_control );
    }
	
    
    /**
     * Inject dependencies.
     *
     * @param model_asset to view initially
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     */
    public JGenericAssetView (  AssetModel  model_asset,
                                AssetRetriever m_retriever,
                                IconLibrary lib_icon    ) {
        om_retriever = m_retriever;
        olib_icon = lib_icon;
        owlink_acl = new JAssetLink ( olib_icon );
        owlink_to = new JAssetLink ( olib_icon );
        owlink_from = new JAssetLink ( olib_icon );
        owlink_owner = new JAssetLink ( olib_icon );
        owlink_home = new JAssetLink ( olib_icon );
        owlink_creator = new JAssetLink ( olib_icon );
        owlink_updater = new JAssetLink ( olib_icon );
        
        oview_util.addPropertyChangeListener ( new PropertyChangeListener () {
            /** Receive events from the View model */
            public void propertyChange ( PropertyChangeEvent evt_prop ) {
                if ( evt_prop.getPropertyName ().equals ( AssetView.Property.assetModel.toString () ) ) {
                    // Model has changed under us
                    SwingUtilities.invokeLater ( 
                                                 new Runnable () {
                                                     public void run () {
                                                         updateAssetUI ();
                                                     }
                                                 }
                                                 );                
                }
            }
        }
                                               );        
        buildAssetUI ();
        setAssetModel ( model_asset );        
    }

	

    /**
     * Trigger a UI sync call to updateAssetUI
     * if the LittleEvent comes from
     * the getAssetModel() AssetModel (data model update).
     */
    protected void eventFromModel ( LittleEvent evt_prop ) {
        if ( evt_prop.getSource () == getAssetModel () ) {
            // Model has changed under us
            SwingUtilities.invokeLater ( 
                                         new Runnable () {
                                             public void run () {
                                                 updateAssetUI ();
                                             }
                                         }
                                         );                
        }
    }

    /**
     * Give subtypes access to SimpleLittleTool for
     * firing PropertyChangeEvents and LittleEvents to
     * registered listeners
     *
     * @return SimpleLittleTool managing listeners.
     */
    protected SimpleLittleTool getEventTool () {
        return oview_util;
    }
    
    /**
     * Give subtypes access to add into the tab-pain.
     *
     * @param i_index may be -1 to just append to end
     */
    protected void insertTab ( String s_title, Icon icon_title, 
                               Component w_tab, String s_tip,
                               int i_index
                               ) {
        if ( i_index < 0 ) {
            i_index = owtab_stuff.getTabCount ();
        }
        owtab_stuff.insertTab ( s_title, icon_title, w_tab, s_tip, i_index );
    }
    
    /**
     * Let subtypes disable their tab
     *
     * @param w_tab component corresponding to the tab to 
     *                 disable/enable
     * @param b_enable set true to enable, false to disable
     */
    protected void setTabEnabled ( Component w_tab, boolean b_enable ) {
        int i_index = owtab_stuff.indexOfComponent ( w_tab );
        if ( i_index > -1 ) {
            owtab_stuff.setEnabledAt ( i_index, b_enable );
        }
    }
    
    
    
    /**
     * Update a label holding Date info
     *
     * @param wlabel_date to update
     * @param t_date to display - may be null
     */
    private void updateLabelInfo ( JLabel wlabel_date, Date t_date ) {
        if ( null == t_date ) {
            wlabel_date.setText ( "<html><i>null</i></html>" );
        } else {
            DateFormat format_date = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" );

            wlabel_date.setText ( format_date.format ( t_date ) );
        }
        wlabel_date.setIcon ( olib_icon.lookupIcon ( "littleware.calendar" ) );
    }

    /**
     * Update the JLabel that shows the current view&apos;s dependence
     * on some other asset.
     *
     * @param wlink_depend the JLabel that needs update
     * @param u_depend id of the asset this depends on - will lookup that
     *                          asset to get its name if possible
     */
    private void updateLabelInfo ( JAssetLink wlink_depend, UUID u_depend ) { 
        wlink_depend.setLink ( u_depend, getAssetModel ().getLibrary (), om_retriever );
    }

    /**
     * Reset the entire UI with fresh data from the active model.
     * Subtypes may extend, but should start out by calling super.updateAssetUI().
     */
    protected void updateAssetUI () {
        // Reconfigure the UI
        Asset a_data = getAssetModel ().getAsset ();
        owlabel_type.setText ( a_data.getAssetType ().toString () );
        owlabel_type.setIcon ( olib_icon.lookupIcon ( a_data.getAssetType () ) );
        
        owlabel_name.setText ( a_data.getName () );
        owlabel_id.setText ( UUIDFactory.makeCleanString ( a_data.getObjectId () ) );
        owlabel_value.setText ( a_data.getValue ().toString () );
        owlabel_transaction.setText ( Long.toString ( a_data.getTransactionCount () ) );
        
        updateLabelInfo ( owlink_acl, a_data.getAclId () );
        updateLabelInfo ( owlink_to, a_data.getToId () );
        updateLabelInfo ( owlink_from, a_data.getFromId () );
        updateLabelInfo ( owlink_home, a_data.getHomeId () );
        updateLabelInfo ( owlink_owner, a_data.getOwnerId () );
        updateLabelInfo ( owlink_creator, a_data.getCreatorId () );
        updateLabelInfo ( owlink_updater, a_data.getLastUpdaterId () );
        
        updateLabelInfo ( owlabel_start, a_data.getStartDate () );
        updateLabelInfo ( owlabel_end, a_data.getEndDate () );
        updateLabelInfo ( owlabel_date_updated, a_data.getLastUpdateDate () );
        updateLabelInfo ( owlabel_date_created, a_data.getCreateDate () );
        
        owtext_comment.setText ( a_data.getComment () );
        owtext_update.setText ( a_data.getLastUpdate () );
        owtext_data.setText ( a_data.getData () );

        this.repaint ();        
    }
    
    
    public AssetModel getAssetModel () {
        return oview_util.getAssetModel ();
    }

    
    public void setAssetModel ( AssetModel model_asset ) {
        oview_util.setAssetModel ( model_asset );
    }
	
	
	public void	addLittleListener( LittleListener listen_little ) {
		oview_util.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		oview_util.removeLittleListener ( listen_little );
	}
	

    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        oview_util.addPropertyChangeListener ( listen_props );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        oview_util.removePropertyChangeListener ( listen_props );
    }
    
    
    /**
     * Allow subtypes to fire events to the listeners managed by this class.
     *
     * @param event_little to propogate to listeners
     */
    protected void fireLittleEvent ( LittleEvent event_little ) {
        oview_util.fireLittleEvent ( event_little );
    }
    

}

