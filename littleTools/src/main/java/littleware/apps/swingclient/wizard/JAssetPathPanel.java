/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.wizard;

import littleware.apps.swingclient.AssetView;
import littleware.apps.swingclient.AssetViewFactory;
import littleware.asset.client.AssetLibrary;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.*;


import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.SimpleAssetViewController;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.event.LittleTool;
import littleware.security.LittleAcl;


/** 
 * Simple wizard panel for specifying some text.
 * Might want to separate out data model and controller later, and add
 * event/listener support, but this is fine for the
 * wizard framework as is.
 */
public class JAssetPathPanel extends JPanel {
    private static final int   OI_FIELDSIZE = 60;
    private static final long serialVersionUID = -4311442963234747531L;

    private final JLabel               ojLabelInstructions = new JLabel( "Instructions" );
    private final JTextField           owtext_info;
    private final AssetSearchManager   om_search;
    private final IconLibrary          olib_icon;
    private final AssetLibrary    olib_asset;
    private final JAssetLink           owlink_asset;
    private final JToolBar             owtbar_control = new JToolBar( "Controls", SwingConstants.HORIZONTAL );
    private final AssetViewFactory     ofactory_view;
    
    private AssetPath                  opath_current = null;
    private UUID                       ou_asset = null;
    private List<AssetType>            ov_legal = new ArrayList<AssetType> ();
    private AssetView                  oview_asset = null;
    private JDialog                    owbrowser_root = null;
    
    private boolean                    ob_gui_built = false;
    private final Provider<JAssetBrowser> oprovideBrowser;
    private final AssetPathFactory opathFactory;
    private final Provider<JSimpleAssetToolbar> oprovideToolbar;
    private final SimpleAssetViewController ocontroller;

    public String getInstructions () {
        return ojLabelInstructions.getText();
    }
    public void setInstructions( String sInstructions ) {
        ojLabelInstructions.setText( sInstructions );
    }

    /**
     * Enumeration of buttons automatically setup in the toolbar.
     * Pass to getButton() to retrieve buttons to listen to.
     * <ul>
     * <li> APPLY - resolve the path in the text-field, and setAssetPath() or
     *           popup error dialog if resolve fails </li>
     * <li> BROWSE - open an asset browser to the current AssetPath </li>
     * </ul>
     */
    private enum ButtonId { 
        APPLY,
        BROWSE
    }
    
    // Handle on the browse button for enable/disable
    private JButton  owbutton_browse = null;
    
    /**
     * Internal util populate this panel with its components
     */
    private void buildUI () {
        if ( ob_gui_built ) {
            return;
        }
        ob_gui_built = true;
        
        final GridBagConstraints gcontrol_panel = new GridBagConstraints ();
        
        gcontrol_panel.gridx = 0;
        gcontrol_panel.gridy = 0;
        gcontrol_panel.gridheight = 1;
        gcontrol_panel.gridwidth = 1;
        //gcontrol_panel.fill = GridBagConstraints.HORIZONTAL;
        
        this.add ( ojLabelInstructions,
                   gcontrol_panel
                   );
        gcontrol_panel.gridy += 1;
        this.add ( owtext_info, gcontrol_panel );
        
        // Go ahead and setup an internal controller 
        for ( ButtonId n_button : ButtonId.values () ) {
            Action  act_button = null;
            
            switch ( n_button ) {
                case APPLY: {
                    act_button = new AbstractAction () {
                    @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            setAssetPathFromUI ();
                        }
                    };
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.apply" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Resolve the AssetPath and apply to editor asset" );
                    
                } break;
                case BROWSE: {
                    act_button = new AbstractAction () {
                        @Override
                        public void actionPerformed ( ActionEvent ev_button ) {
                            launchBrowser ();
                        }
                    };
                    act_button.putValue ( Action.SMALL_ICON, olib_icon.lookupIcon ( "littleware.browse" ) );
                    act_button.putValue ( Action.SHORT_DESCRIPTION, "Browse around the current AssetPath" );
                } break;
                default: throw new AssertionError ( "Unhandled ButtonId: " + n_button );
            }
            
            JButton wbutton_tool = new JButton ( act_button );
            owtbar_control.add ( wbutton_tool );
            
            if ( n_button.equals ( ButtonId.BROWSE ) ) {
                owbutton_browse = wbutton_tool;
            }
        }
        gcontrol_panel.gridx += 1;
        this.add ( owtbar_control, gcontrol_panel );
        gcontrol_panel.gridx = 0;
    }
    
    /**
     * Center the browser associated with this thing on the
     * currently applied AssetPath, and set it visible.
     * Should call this from the Swing dispatch thread.
     */
    private void launchBrowser () {
        if ( null == oview_asset ) {
            JAssetBrowser wbrowser_asset = oprovideBrowser.get();

            ocontroller.setControlView((AssetView) wbrowser_asset );
            final JSimpleAssetToolbar wtoolbar_asset = oprovideToolbar.get();
            wtoolbar_asset.setConnectedView( wbrowser_asset );
            ((LittleTool) wtoolbar_asset).addLittleListener ( ocontroller );
                        
            final JPanel              wpanel_buttons = new JPanel ();
            final JPanel              wpanel_browser = new JPanel ( new GridBagLayout () );
            final GridBagConstraints  gcontrol_browser = new GridBagConstraints ();
            final JButton wbutton_select = new JButton ( "Select" );
            
            wbutton_select.addActionListener ( new ActionListener () {
                @Override
                public void actionPerformed( ActionEvent evt_action ) {
                    try {
                        UUID u_asset = oview_asset.getAssetModel ().getAsset ().getId ();
                        setAssetPath ( opathFactory.createPath ( u_asset ) );
                        owbrowser_root.setVisible( false );
                    } catch ( Exception e ) {
                        JOptionPane.showMessageDialog( JAssetPathPanel.this, 
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
            
            
            //wpanel_browser.setPreferredSize ( new Dimension ( 1000, 700 ) );
            
            owbrowser_root = new JDialog ();
            owbrowser_root.setModal(true);
            owbrowser_root.getContentPane ().add ( wpanel_browser );
            oview_asset = (AssetView) wbrowser_asset;
            owbrowser_root.pack ();
        }
        try {
            // Where should we start browsing ?
            if ( null != getAssetId () ) {
                oview_asset.setAssetModel ( olib_asset.retrieveAssetModel ( getAssetId (), om_search ).get() );
            } else {
                // just start browsing around ACL_EVERYBODY
                oview_asset.setAssetModel ( olib_asset.syncAsset ( om_search.getByName ( LittleAcl.ACL_EVERYBODY_READ,
                                                                                           LittleAcl.ACL_TYPE ).get()
                                                                   )
                                            );
            }
            owbrowser_root.setVisible( true );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( JAssetPathPanel.this, 
                                           "Could not launch browser around " + getAssetId () +
                                           ", caught: " + e, "alert", 
                                           JOptionPane.ERROR_MESSAGE
                                           );            
        }

    }
    
    /**
     * Attempt the resolve the text-field data to a valid
     * AssetPath.  Popup an ErrorDialog on failure.
     */
    private void setAssetPathFromUI () {
        try {
            setAssetPath ( getText () );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( JAssetPathPanel.this, 
                                           "Could not resolve AssetPath: " + getText () +
                                          ", caught: " + e, "alert", 
                                          JOptionPane.ERROR_MESSAGE
                                          );
        }
    }
    
    /**
     * Construct the panel 
     *
     * @param s_short_instruction short instruction to populate a JLabel with
     * @param v_legal set of legal asset types - may be null to leave unrestricted
     * @param factory_view AssetView component factory
     */
    @Inject
    public JAssetPathPanel ( Provider<JAssetBrowser> provideBrowser,
                             AssetSearchManager m_search,
                             AssetLibrary  lib_asset,
                             IconLibrary        lib_icon,
                             AssetViewFactory   factory_view,
                             Provider<JAssetLink>  provideLink,
                             AssetPathFactory      pathFactory,
                             Provider<JSimpleAssetToolbar> provideToolbar,
                             SimpleAssetViewController controller
                             ) {
        super( new GridBagLayout () );
        oprovideBrowser = provideBrowser;
        owtext_info = new JTextField ( OI_FIELDSIZE );
        om_search = m_search;
        olib_icon = lib_icon;
        olib_asset = lib_asset;
        owlink_asset = provideLink.get();
        ofactory_view = factory_view;
        opathFactory = pathFactory;
        oprovideToolbar = provideToolbar;
        ocontroller = controller;
        buildUI ();
    }
    
    
    /**
     * Get the text currently entered in the UI AssetPath textfield.
     * May not correspond with the current AssetPath - the
     * user needs to "Apply" the textfield to set the AssetPath.
     */
    public String getText () {
        return owtext_info.getText ();
    }
    
    
    /**
     * Get the asset-path
     */
    public AssetPath getAssetPath () {
        return opath_current;
    }
    
    /** 
     * Set the asset-path.
     *
     * @param path_asset to set to after verification - may be null
     * @throws GeneralSecurityException if unable to resolve AssetPath due to security
     * @throws InvalidAssetTypeException if AssetPath resolves to an Asset 
     *                not of legal type for this Panel.
     */
    public void setAssetPath ( AssetPath path_asset 
                               ) throws BaseException, GeneralSecurityException, RemoteException
    {
        if ( null == path_asset ) {
            owtext_info.setText ( "" );
            ou_asset = null;
            opath_current = null;
            //owbutton_browse.setEnabled ( false );
            return;
        }
        final Asset a_link = om_search.getAssetAtPath ( path_asset ).get();
        olib_asset.syncAsset( a_link );
        if ( (! ov_legal.isEmpty ())
             && (! ov_legal.contains ( a_link.getAssetType () ))
             ) {
            throw new InvalidAssetTypeException ( "Asset type not in legal set: " + a_link.getAssetType () );
        }

        opath_current = path_asset;
        ou_asset = a_link.getId ();
        owtext_info.setText ( path_asset.toString () );
        //owbutton_browse.setEnabled( true );
    }
    
    /** 
     * Set the asset-path.
     *
     * @param s_path string path to parse then set
     * @throws GeneralSecurityException if unable to resolve AssetPath due to security
     * @throws InvalidAssetTypeException if AssetPath resolves to an Asset 
     *                not of legal type for this Panel.
     */
    public void setAssetPath ( String s_path
                               ) throws BaseException, GeneralSecurityException, RemoteException
    {
        if ( (null == s_path) || "".equals( s_path ) ) {
            setAssetPath( (AssetPath) null );
        } else {
            setAssetPath ( opathFactory.createPath ( s_path ) );
        }
    } 
    
    
    /**
     * Get the set of legal asset-types to restrict the user selection to.
     * Empty implies no restrictions on type.
     * Listed in order of display.
     */
    public List<AssetType> getLegalAssetType () {
        return ov_legal;
    }
    public void setLegalAssetType ( List<AssetType> v_legal ) {
        ov_legal = v_legal;
    }
    
    /**
     * Get the asset-id that the last applied AssetPath resolves to.
     * May be null if AssetPath is null.
     */
    public UUID getAssetId () {
        return ou_asset;
    }
    
    /**
     * Forwards focus request to text-field
     */
    @Override
    public void requestFocus () {
        owtext_info.requestFocus ();
    }
    
}
