package littleware.apps.swingclient;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.beans.PropertyChangeListener;
import java.util.UUID;

import javax.swing.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.controller.SimpleAssetViewController;
import littleware.apps.swingclient.event.SelectAssetEvent;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.AssetSearchManager;


/**
 * Wrapper of JAssetLink with a button
 * that launches a browser-dialog thing by 
 * which the client can request to change the link.
 * Fires a SelectAssetEvent when user selects
 * a new value.
 */
public class JAssetLinkEditor extends JPanel implements LittleTool {
    private final SimpleLittleTool     otool_support = new SimpleLittleTool ( this );
    private final JAssetLink           owalink_edit;
    private final AssetSearchManager   om_search;  
    private final IconLibrary          olib_icon;
    private final AssetModelLibrary    olib_asset;
    private final AssetViewFactory     ofactory_view;
    
    private       AssetView            oview_browser = null;
    private       JDialog              owdial_browser = null;
    private       AssetModel           oamodel_fallback = null;    
    
    
    /**
     * Setup the link with the library to render icons
     *
     * @param m_search to browse with
     * @param lib_asset model library
     * @param lib_icon source of icons
     * @param amodel_fallback asset to start the editor
     *         browsing at if the link is set to null  
     * @param factory_view view-factory for browsing in search of new assets
     */
    public JAssetLinkEditor ( AssetSearchManager m_search, 
                              AssetModelLibrary lib_asset,
                              IconLibrary lib_icon,
                              AssetModel amodel_fallback,
                              AssetViewFactory factory_view
                        ) {
        super( new FlowLayout ( FlowLayout.LEFT ) );
        owalink_edit = new JAssetLink ( lib_icon );
        owalink_edit.setToolTipText ( null );
        olib_asset = lib_asset;
        olib_icon = lib_icon;
        om_search = m_search;
        oamodel_fallback = amodel_fallback;
        ofactory_view = factory_view;
        
        JButton  wbutton_browse = new JButton ( lib_icon.lookupIcon ( "littleware.browse" ) );
        wbutton_browse.addActionListener (
                                          new ActionListener () {
                                              public void actionPerformed ( ActionEvent ev_button ) {
                                                  openBrowser ();
                                              }
                                          }
                                          );

        this.add ( owalink_edit );
        this.add ( wbutton_browse );
    }
    
    /**
     * Internal utility to open the browser
     */
    private void openBrowser () {
        if ( null == owdial_browser ) {
            // build the browser
            JComponent wbrowser_asset = new JAssetBrowser (  ofactory_view,
                                                             olib_icon,
                                                             olib_asset,
                                                             om_search
                                                             );
            
            SimpleAssetViewController  listen_control = new SimpleAssetViewController ( 
                                                                             om_search,
                                                                             olib_asset
                                                                             );
            listen_control.setControlView( (AssetView) wbrowser_asset );
            JSimpleAssetToolbar wtoolbar_asset = new JSimpleAssetToolbar ( 
                                                                  olib_asset,
                                                                  olib_icon,
                                                                  om_search
                                                                  );
            wtoolbar_asset.setConnectedView( (AssetView) wbrowser_asset );
            ((LittleTool) wtoolbar_asset).addLittleListener ( listen_control );
            
            final JPanel              wpanel_buttons = new JPanel ();
            final JPanel              wpanel_browser = new JPanel ( new GridBagLayout () );
            final GridBagConstraints  gcontrol_browser = new GridBagConstraints ();
            final JButton wbutton_select = new JButton ( "Select" );
            
            wbutton_select.addActionListener ( new ActionListener () {
                public void actionPerformed( ActionEvent evt_action ) {
                    try {
                        Asset a_selected = oview_browser.getAssetModel ().getAsset ();
                        otool_support.fireLittleEvent ( new SelectAssetEvent ( JAssetLinkEditor.this, a_selected ) );
                        owdial_browser.setVisible( false );
                    } catch ( Exception e ) {
                        JOptionPane.showMessageDialog( JAssetLinkEditor.this, 
                                                       "Unexpected failure selecting asset, caught: " + e,
                                                       "alert", 
                                                       JOptionPane.ERROR_MESSAGE
                                                       );        
                    }
                }
            }
                                               );
            
            
            wpanel_buttons.add ( wtoolbar_asset );
            wpanel_buttons.add ( wbutton_select );
            
            
            gcontrol_browser.anchor = GridBagConstraints.FIRST_LINE_START;
            gcontrol_browser.gridx = 0;
            gcontrol_browser.gridy = 0;
            gcontrol_browser.gridwidth = GridBagConstraints.REMAINDER;
            gcontrol_browser.gridheight = 1;
            gcontrol_browser.weightx = 0.5;
            gcontrol_browser.fill = GridBagConstraints.HORIZONTAL;
            
            //wpanel_browser.add ( wtoolbar_asset, gcontrol_browser );
            wpanel_browser.add ( wpanel_buttons, gcontrol_browser );
            
            gcontrol_browser.gridx = 0;
            gcontrol_browser.gridy += gcontrol_browser.gridheight;
            gcontrol_browser.weighty = 0.5;
            gcontrol_browser.weightx = 0.5;
            gcontrol_browser.gridwidth = GridBagConstraints.REMAINDER;
            gcontrol_browser.gridheight = GridBagConstraints.REMAINDER;
            gcontrol_browser.fill = GridBagConstraints.BOTH;
            wpanel_browser.add ( wbrowser_asset, gcontrol_browser );
            
            owdial_browser = new JDialog ();
            owdial_browser.setTitle ( "Link Editor" );
            
            owdial_browser.getContentPane ().add ( wpanel_browser );
            oview_browser = (AssetView) wbrowser_asset;
            owdial_browser.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big            
            owdial_browser.pack ();            
        }
        if ( null != getLink () ) {
            try {
                oview_browser.setAssetModel ( olib_asset.retrieveAssetModel ( getLink (), om_search ) );
            } catch ( Exception e ) {
                oview_browser.setAssetModel ( oamodel_fallback );
            }
        } else {
            oview_browser.setAssetModel ( oamodel_fallback );
        }
        
        owdial_browser.setVisible ( true );
    }

	public void	addLittleListener( LittleListener listen_action ) {
        otool_support.addLittleListener ( listen_action );
    }
	

	public void     removeLittleListener( LittleListener listen_action ) {
        otool_support.removeLittleListener ( listen_action );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        otool_support.addPropertyChangeListener ( listen_props );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        otool_support.removePropertyChangeListener ( listen_props );
    }

    /**
     * Set the link info - retrieve the asset with u_id,
     * register it with the model-library, and
     * setup the display.  Eats checked exceptions, and just
     * sets up the display with bomb/error info.
     * Also sets the fallback asset to a_linkto if it's not null.
     * 
     * @see littleware.web.apps.swingclient.JAssetLink
     * @param u_id to display - may be null - ignores other args if null
     */
    public void setLink ( UUID u_id ) {
        owalink_edit.setLink ( u_id, olib_asset, om_search );
        if ( null != u_id ) {
            setFallbackAsset ( olib_asset.get ( u_id ) );
        }
    }
    
    /**
     * Set the link to point at the given asset.
     * Also sets the fallback asset to a_linkto if it's not null.
     *
     * @see littleware.web.apps.swingclient.JAssetLink     
     * @param a_linkto asset to link to - may be null
     */
    public void setLink ( Asset a_linkto ) {
        owalink_edit.setLink ( a_linkto );
        if ( null != a_linkto ) {
            setFallbackAsset ( olib_asset.get ( a_linkto.getObjectId () ) );
        }
    }
    
    /**
     * Get the id of the asset the link points to - may be null,
     * may be an id of a "bomb" asset the user cannot access/load.
     *
     * @see littleware.web.apps.swingclient.JAssetLink     
     */
    public UUID getLink () {
        return owalink_edit.getLink ();
    }

    /**
     * Get the current fallback asset to start browsing at if the link is inaccessible
     */
    public AssetModel getFallbackAsset () {
        return oamodel_fallback;
    }
    
    /**
     * Set the fallback asset.  NOOP if NULL
     */
    public void setFallbackAsset ( AssetModel amodel_fallback ) {
        if ( null != amodel_fallback ) {
            oamodel_fallback = amodel_fallback;
        }
    }
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

