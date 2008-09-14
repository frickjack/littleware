package littleware.web.applet;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.net.URL;
import java.util.*;

import javax.swing.*;         
import javax.swing.text.JTextComponent;


import littleware.base.*;
import littleware.base.swing.*;
import littleware.security.auth.*;
import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.GroupFolderTool;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.security.SecurityAssetType;

/**
 * Applet implementation of the JGroupsUnderParent control.
 * Expects the following applet parameters: <br />
 *     session_uuid: id of the user session to connect to <br />
 *     asset_uuid: id of folder asset to edit groups in, default to session_uuid if not set <br />
 */
public class GroupTool extends Toolbox {
    /** Name of the groups folder */
    public static final String   OS_GROUPS_FOLDER = "GroupsFolder";
    
    private   Asset                 oa_groups_folder = null;
    private   DefaultListModel      omodel_grouplist = new DefaultListModel ();
    private   JFrame                owframe_tool = new JFrame ();
    private   JTextComponent        owtext_newgroup = new JTextField ( 50 );
    
    /**
     * Enable the create/edit buttons in the underlying UI
     * for GROUP assets.
     */
    private void buildUI () {
        try {
            final SessionHelper          m_helper = getSessionHelper ();
            final AssetSearchManager     m_search = m_helper.getService ( ServiceType.ASSET_SEARCH );
            final AssetManager           m_asset = m_helper.getService ( ServiceType.ASSET_MANAGER );
            final AssetModelLibrary      lib_asset = getAssetModelLibrary ();
            final IconLibrary            lib_icon = getIconLibrary ();
            final AssetViewFactory       factory_view = getViewFactory ();
            final Asset                  a_user = lib_asset.retrieveAssetModel ( m_helper.getSession ().getCreatorId (), m_search ).getAsset ();
            Asset                        a_groups = m_search.getAssetFromOrNull ( a_user.getObjectId (), OS_GROUPS_FOLDER );
            
            // Create the groups folder if it does not exist
            if ( null == a_groups ) {
                a_groups = SecurityAssetType.GENERIC.create ();
                a_groups.setOwnerId ( a_user.getObjectId () );
                a_groups.setAclId ( m_search.getByName ( littleware.security.AclManager.ACL_EVERYBODY_READ, 
                                                              SecurityAssetType.ACL ).getObjectId () 
                                    );
                a_groups.setComment ( "User folder for groups" );
                a_groups.setFromId ( a_user.getObjectId () );
                a_groups.setHomeId ( a_user.getHomeId () );
                a_groups.setName ( OS_GROUPS_FOLDER );
                a_groups = m_asset.saveAsset ( a_groups, "Setup GROUPS folder under user asset: " + a_user.getName () );
            }

            oa_groups_folder = a_groups;
            
            GroupFolderTool grouptool_test = GroupFolderTool.create ( lib_asset.syncAsset ( oa_groups_folder ),
                                                                      m_asset,
                                                                      m_search, 
                                                                      factory_view,
                                                                      lib_icon
                                                                      );
            
            owframe_tool.getContentPane ().setLayout ( new GridBagLayout () );            
            GridBagConstraints   gcontrol_main = new GridBagConstraints ();
            gcontrol_main.gridx = 0;
            gcontrol_main.gridy = 0;
            gcontrol_main.anchor = GridBagConstraints.LINE_START;
            gcontrol_main.gridwidth = 1;
            gcontrol_main.gridheight = 1;
            //gcontrol_main.fill = GridBagConstraints.HORIZONTAL;
            
            owframe_tool.add ( grouptool_test.getToolbar (), gcontrol_main );
            
            gcontrol_main.gridy += gcontrol_main.gridheight;
            gcontrol_main.weighty = 0.5;
            gcontrol_main.fill = GridBagConstraints.BOTH;
            gcontrol_main.gridwidth = 3;
            gcontrol_main.gridheight = 5;
            
            owframe_tool.add ( grouptool_test.getGroupsView (), gcontrol_main );
            //owframe_tool.setPreferredSize ( new Dimension ( 400, 500 ) );  // force big
            

            owframe_tool.pack ();
            
            //------------------------------------
            JButton  wbutton_launch = new JButton ( "Show GroupTool" );
            wbutton_launch.addActionListener ( new ActionListener () {
                public void actionPerformed ( ActionEvent event_launch ) {
                    owframe_tool.setVisible ( true );
                }
            }
                                               );
            getContentPane ().add ( wbutton_launch );                                                                             
            
        } catch ( Exception e ) {
            getContentPane ().add ( new JLabel ( "<html><body><font color=\"red\">Failed to establish remote session, caught: " +
												 XmlSpecial.encode ( e.toString () ) +
												 "</font></body></html>"
												 ),
									BorderLayout.CENTER
									);
        }
    }
    
    
	/**
     * Buildup the browser widgets
	 */
    public void init() {
        super.init ();
        try {
            SwingUtilities.invokeAndWait ( new Runnable () {
                public void run () {
                    buildUI ();
                }
            }
                                           );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new AssertionFailedException ( "Failed to build UI", e );
        }
    }
	
    
	public void start() {
	}
    
	public void stop() {
	}
    
	public void destroy() {
	}
    

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

