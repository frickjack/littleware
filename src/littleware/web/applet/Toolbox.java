package littleware.web.applet;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.*;

import javax.swing.*; 
import javax.swing.border.Border;

import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.*;
import littleware.base.swing.*;
import littleware.security.auth.*;
import littleware.security.SecurityAssetType;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.*;

/**
 * Abstract applet baseclass setups a lame dependency injection mechanism.
 * Accesses various applet parameters to define dependencies.
 * <dl>
 * <dt>session_uuid</dt>
 *     <dd> required parameter specifies the session_uuid the applet
 *               should establish an RMI connection via </dd>
 * <dt>icon_root</dt>
 *     <dd>specifies the path to the WebIconLibrary icon directory
 *         structure - http://host/icon_root/lib/icons/-
 *         </dd>
 * <dt>session_language</dt>
 *     <dd> specify the locale under which the applet should operate </dd>
 * <dt>asset_uuid</dt>
 *      <dd> specify the id of an applet that the applet should 
 *             initially operate upon </dd>
 * </dl>
 */
public class Toolbox extends JApplet {
    private static final Logger  olog_generic = Logger.getLogger ( "littleware.web.applet.Toolbox" );
    
    /**
     * Stash the session-manager.
     * Initialize first time Toolbox init() method called.
     * We assume all applets come from the same codebase.
     */
    private static SessionManager    om_session = null;
    /**
     * Stash icon library.
     * Initialize first time Toolbox init() method called.
     * We assume all applets come from the same codebase.
     */    
    private static IconLibrary       olib_icon = null;

    
    /**
     * Cache session data.  Allow client to have multiple independent sessions.
     */
    private final static Map<UUID,Toolbox>  ov_cache = new HashMap<UUID,Toolbox> ();
    
    /**
     * Initialize the given Toolbox Applet with cached values
     * stashed in static space.
     * Setup tbox_init internal values - including ob_session_ok,
     * otherwise throw exception and leave ob_session_ok false.
     *
     * @param tbox_init toolbox to init - must have "session_uuid" applet-parameter set
     * @exception AppletParameterException if session_uuid not set
     */
    private static synchronized void setupSession ( 
                                                    Toolbox  tbox_init
                                                    ) throws BaseException, GeneralSecurityException,
        RemoteException, AppletParameterException, 
        java.net.MalformedURLException, java.rmi.NotBoundException
    {
        URL            url_codebase = tbox_init.getCodeBase ();
        String         s_uuid_session = tbox_init.getParameter ( "session_uuid" );
        String         s_uuid_asset = tbox_init.getParameter ( "asset_uuid" );
        String         s_locale = tbox_init.getParameter ( "session_language" );
        String         s_icon_root = tbox_init.getParameter( "icon_root" );
        
        olog_generic.log ( Level.INFO, "Setting up session" );
        if ( null != s_uuid_asset ) {
            tbox_init.ou_asset = UUIDFactory.parseUUID ( s_uuid_asset );
        }
        if ( null == s_uuid_session ) {
            throw new AppletParameterException ( "required session_uuid parameter not set" );
        }     
        if ( null != s_locale ) {
            tbox_init.setLocale( new Locale ( s_locale ) );
        }   
        if ( null == s_icon_root ) {
            s_icon_root = "/littleware/lib/icons";
        }
        
        UUID                  u_session = UUIDFactory.parseUUID ( s_uuid_session );
        
        Toolbox tbox_cache = ov_cache.get ( u_session );
        if ( null != tbox_cache ) {
            if ( tbox_cache == tbox_init ) {
                throw new AssertionFailedException ( "Multiple applet inits" );
            }
            olog_generic.log ( Level.INFO, "Session-cache hit initializing Toolbox applet" );
            tbox_init.olib_asset = tbox_cache.olib_asset;
            tbox_init.ofactory_view = tbox_cache.ofactory_view;
            tbox_init.ofactory_editor = tbox_cache.ofactory_editor;
            tbox_init.om_helper = tbox_cache.om_helper;
            if ( null == s_locale ) {
                tbox_init.setLocale ( tbox_cache.getLocale () );
            }
            tbox_init.ob_session_ok = true;
            return;
        }
        

        if ( null == olib_icon ) {
            String s_full_icon_root = url_codebase.getHost () + s_icon_root;
            olog_generic.log ( Level.INFO, "Setting up icon library referencing: " +
                    s_full_icon_root
                    );
            olib_icon = new WebIconLibrary ( s_full_icon_root );
        }
        if ( null == om_session ) {
            olog_generic.log ( Level.INFO, "Trying to contact RMI registry on host: " +
                    url_codebase.getHost ()
                    );
            om_session = SessionUtil.getSessionManager ( url_codebase.getHost (), 
                                                          SessionUtil.getRegistryPort () 
                                                           );
        }                    

        
        tbox_init.olib_asset = new SimpleAssetModelLibrary ();
        tbox_init.om_helper = om_session.getSessionHelper ( u_session );
        
        AssetSearchManager    m_search = tbox_init.om_helper.getService ( ServiceType.ASSET_SEARCH );
        AssetManager          m_asset = tbox_init.om_helper.getService ( ServiceType.ASSET_MANAGER );
        
        tbox_init.ofactory_view = new SimpleAssetViewFactory ( m_search, olib_icon );
        tbox_init.ofactory_editor = new EditorAssetViewFactory ( m_asset, m_search, olib_icon, tbox_init.ofactory_view );
        tbox_init.ob_session_ok = true;
        ov_cache.put ( u_session, tbox_init );
    }
    
    private AssetModelLibrary     olib_asset = null;
    private AssetViewFactory      ofactory_view = null;
    private AssetEditorFactory    ofactory_editor = null;
    private SessionHelper         om_helper = null;
    private boolean               ob_session_ok = false;
    // asset_uuid parameter value
    private UUID                  ou_asset = null;
    private JAssetBrowser         owbrowser_asset = null;
    
    private final JFrame        owframe_browser = new JFrame ();
    private final JFrame        owframe_gtool = new JFrame ();
    private final JFrame        owframe_logger = new JFrame ();
    private final JTextArea     owtext_advise = new JTextArea ( "Ok", 3, 30 );
    private final JTextAppender owappender_log = new JTextAppender ( JTextAppender.OI_DEFAULT_HEIGHT, 
                                                         JTextAppender.OI_DEFAULT_WIDTH, 102400 );
    // Setup a log-handler to route log messages to a log window too!    
    private final Handler ologhandle_2ui;
    
    {
        owframe_logger.getContentPane ().add ( owappender_log );
        owframe_logger.pack ();
        
        final Formatter  format_log = new SimpleFormatter ();
        ologhandle_2ui = new Handler () {
            public void close () {}
            public void flush () {}
            public void publish ( LogRecord record_log ) {
                owappender_log.append ( format_log.format ( record_log ) );
            }
        };
    }
    
        
    
    /** Allow subtypes to get handle on AssetModelLibrary */
    protected AssetModelLibrary getAssetModelLibrary () {
        return olib_asset;
    }
    
    /** Allow subtypes to get handle on IconLibrary */
    protected IconLibrary getIconLibrary () {
        return olib_icon;
    }
    
    /** Allow subtypes to get handle on SessionHelper */
    protected SessionHelper getSessionHelper () {
        return om_helper;
    }
    
    /**
     * Get the UUID that the "asset_uuid" applet parameter resolves to -
     * may be null
     */
    protected UUID getRequestedAssetId () {
        return ou_asset;
    }
    
    /** Allow subtypest to get the AssetViewFactory */
    protected AssetViewFactory getViewFactory () {
        return ofactory_view;
    }
    
    /** Allow subtyeps to get the AssetEditorFactory */
    protected AssetEditorFactory getEditorFactory () {
        return ofactory_editor;
    }
    
    /**
     * Setup the UI components for the AssetBrowser in the given frame
     */
    private void setupBrowserTool ( Frame wframe_tool ) 
        throws BaseException, RemoteException, GeneralSecurityException 
    {        
        AssetSearchManager    m_search = om_helper.getService ( ServiceType.ASSET_SEARCH );
        AssetManager          m_asset = om_helper.getService ( ServiceType.ASSET_MANAGER );                

        owbrowser_asset = new JAssetBrowser ( ofactory_view, olib_icon, olib_asset, m_search );

        LittleListener  listen_control = new ExtendedAssetViewController ( (AssetView) owbrowser_asset,
                                                                           m_search,
                                                                           m_asset,
                                                                           olib_asset,
                                                                           ofactory_editor, ofactory_view,
                                                                           olib_icon,
                                                                           om_helper.getSession ()
                                                                           );
        owbrowser_asset.addLittleListener ( listen_control );
        JSimpleAssetToolbar wtoolbar_asset = new JSimpleAssetToolbar ( owbrowser_asset,
                                                                       olib_asset,
                                                                       olib_icon,
                                                                       m_search,
                                                                       "Browser Toolbar"
                                                                       );
        wtoolbar_asset.getButton ( JSimpleAssetToolbar.ButtonId.CREATE ).setEnabled ( true );
        wtoolbar_asset.getButton ( JSimpleAssetToolbar.ButtonId.EDIT ).setEnabled ( true );
        wtoolbar_asset.getButton ( JSimpleAssetToolbar.ButtonId.DELETE ).setEnabled ( true );
        
        wtoolbar_asset.addLittleListener ( listen_control );
        
        UUID u_requested = getRequestedAssetId ();
        if ( null != u_requested ) {
            owbrowser_asset.setAssetModel ( olib_asset.retrieveAssetModel ( u_requested, m_search ) );
        } else {
            owbrowser_asset.setAssetModel ( olib_asset.syncAsset ( om_helper.getSession () ) );
        }
        
        wframe_tool.setLayout ( new BorderLayout () );
        wframe_tool.add ( wtoolbar_asset, BorderLayout.PAGE_START );
        wframe_tool.add ( owbrowser_asset, BorderLayout.CENTER );
        wframe_tool.pack (); 
    }
        
    
    /**
     * Setup the UI components for the GroupTool in the given frame
     */
    private void setupGroupTool ( Frame wframe_tool ) 
        throws BaseException, RemoteException, GeneralSecurityException 
    {
        AssetSearchManager    m_search = om_helper.getService ( ServiceType.ASSET_SEARCH );
        AssetManager          m_asset = om_helper.getService ( ServiceType.ASSET_MANAGER );                
        final Asset           a_user = olib_asset.retrieveAssetModel ( om_helper.getSession ().getCreatorId (), m_search ).getAsset ();        
        Asset           a_groups = m_search.getAssetFromOrNull ( a_user.getObjectId (), GroupTool.OS_GROUPS_FOLDER );
        
        // Create the groups folder if it does not exist
        if ( null == a_groups ) {
            a_groups =  AssetType.GENERIC.create ();
            a_groups.setOwnerId ( a_user.getObjectId () );
            a_groups.setAclId ( m_search.getByName ( littleware.security.AclManager.ACL_EVERYBODY_READ, 
                                                     SecurityAssetType.ACL ).getObjectId () 
                                );
            a_groups.setComment ( "User folder for groups" );
            a_groups.setFromId ( a_user.getObjectId () );
            a_groups.setHomeId ( a_user.getHomeId () );
            a_groups.setName ( GroupTool.OS_GROUPS_FOLDER );
            a_groups = m_asset.saveAsset ( a_groups, "Setup GROUPS folder under user asset: " + a_user.getName () );
        }
                
        GroupFolderTool grouptool_test = GroupFolderTool.create ( olib_asset.syncAsset ( a_groups ),
                                                                  m_asset,
                                                                  m_search, 
                                                                  ofactory_view,
                                                                  olib_icon
                                                                  );
        
        wframe_tool.setLayout ( new GridBagLayout () );            
        GridBagConstraints   gcontrol_main = new GridBagConstraints ();
        gcontrol_main.gridx = 0;
        gcontrol_main.gridy = 0;
        gcontrol_main.anchor = GridBagConstraints.LINE_START;
        gcontrol_main.gridwidth = 1;
        gcontrol_main.gridheight = 1;
        //gcontrol_main.fill = GridBagConstraints.HORIZONTAL;
        
        wframe_tool.add ( grouptool_test.getToolbar (), gcontrol_main );
        
        gcontrol_main.gridy += gcontrol_main.gridheight;
        gcontrol_main.weighty = 0.5;
        gcontrol_main.fill = GridBagConstraints.BOTH;
        gcontrol_main.gridwidth = 3;
        gcontrol_main.gridheight = 5;
        
        wframe_tool.add ( grouptool_test.getGroupsView (), gcontrol_main );
        //wframe_tool.setPreferredSize ( new Dimension ( 400, 500 ) );  // force big
        
        
        wframe_tool.pack ();        
    }
    
    /**
     * Internal UI setup.
     * On exit the owtext_advise will be visible regardless of exception.
     */
    private void setupGUI ()
        throws BaseException, RemoteException, GeneralSecurityException 
    {        
        this.setLayout ( new GridBagLayout () );
        GridBagConstraints  gconst_applet = new GridBagConstraints ();
        gconst_applet.gridx = 0;
        gconst_applet.gridy = 0;
        gconst_applet.gridwidth = 1;
        gconst_applet.gridheight = 1;
        
        try {
            if ( ob_session_ok ) {
                setupGroupTool ( owframe_gtool );
                setupBrowserTool ( owframe_browser );
            
                final JButton    wbutton_launch = new JButton ( "Launch" );
                final JComboBox  wcombo_tools = new JComboBox ( Tool.values () );
                
                wbutton_launch.addActionListener ( new ActionListener () {
                    public void actionPerformed ( ActionEvent ev_button ) {
                        Tool ntool_selected = (Tool) wcombo_tools.getSelectedItem ();
                        ntool_selected.setVisible ( Toolbox.this, true );
                    }
                }
                                                   );
                this.getContentPane ().add ( wcombo_tools, gconst_applet );        
                gconst_applet.gridy += gconst_applet.gridheight;
                this.getContentPane ().add ( wbutton_launch, gconst_applet );
                gconst_applet.gridy += gconst_applet.gridheight;
            }
        } finally {
            gconst_applet.gridheight = 4;
            
            Border border_text =  BorderFactory.createTitledBorder( BorderFactory.createLoweredBevelBorder(),
                                                                    "Toolbox status"
                                                                    );

            JPanel wpanel_text = new JPanel ();
            wpanel_text.setBorder ( border_text );
            wpanel_text.add ( owtext_advise );

            this.getContentPane ().add ( wpanel_text, gconst_applet );
        }
    }
    
	/**
     * Buildup the data needed by the getter methods.
     * Requires the "session_uuid" applet parameter to be set.
     * Also accepts optional "session_language" applet parameter.
     * Call this first!
	 */
    public void init() {	
        if ( null != om_helper ) {
            return;
        }
        try {
            setupSession ( this );
        } catch ( Exception e ) {
            // ignore exception - let setupGUI key on this.ob_session_ok below
            owtext_advise.setText ( "Failed to setup RMI session\nMust LOGIN to run Toolbox" );
        }
        
        try {
            if ( SwingUtilities.isEventDispatchThread () ) {
                setupGUI ();
            } else {
                SwingUtilities.invokeAndWait ( new Runnable () {
                    public void run () {
                        try {
                            setupGUI ();
                        } catch ( RuntimeException e ) {
                            throw e;
                        } catch ( Exception e ) {
                            olog_generic.log ( Level.SEVERE, "Failed setup, caught: " + e + 
                                               ", " + BaseException.getStackTrace ( e )
                                               );
                            throw new AssertionFailedException ( "Setup failed: " + e, e );
                        }
                    }
                }
                                               );
            }                    
        } catch ( Exception e ) {
            olog_generic.log ( Level.SEVERE, "Failed setup (2), caught: " + e + 
                               ", " + BaseException.getStackTrace ( e )
                               );
            if ( ob_session_ok ) {
                owtext_advise.setText ( "Toolbox launch canceled.\nFailed to build UI\nCaught: " + e );
            }
        }  
    }
        

    /**
     * Register our log-handler with the root logger
     */
	public void start() {
        if ( ob_session_ok ) { // valid session setup
            try {
                Logger.getLogger ( "" ).addHandler ( ologhandle_2ui );
            } catch ( SecurityException e ) {
                owtext_advise.setText ( "Security limited access\n... please update .java.policy" );
                owappender_log.append ( "Unable to register UI log handler due to security constrained environment,\n" );
                owappender_log.append ( "...caught: " + e + "\n" );
                owappender_log.append ( "...please update your java.policy file\n    to grant access permissions to this site." );
            }
        }
	}

    /**
     * Unregister our log-handler from the root logger
     */
	public void stop() {
        if ( ob_session_ok ) {
            try {
                Logger.getLogger ( "" ).removeHandler ( ologhandle_2ui );
            } catch ( SecurityException e ) {
            }
        }
	}

	public void destroy() {
	}
    
    /**
     * Enumeration of tools in the Toolbox
     */
    public enum Tool {
        LogViewer {
            public void setVisible ( Toolbox tbox_caller, boolean b_visible ) {
                olog_generic.log ( Level.INFO, "Tool.setVisible: " + this + ", " + b_visible );
                tbox_caller.owframe_logger.setVisible ( true );
            }            
        }, 
        AssetBrowser {
            public void setVisible ( Toolbox tbox_caller, boolean b_visible ) {
                olog_generic.log ( Level.INFO, "Tool.setVisible: " + this + ", " + b_visible );
                tbox_caller.owframe_browser.setVisible ( true );
            }                        
        }, 
        GroupTool {
            public void setVisible ( Toolbox tbox_caller, boolean b_visible ) {
                super.setVisible ( tbox_caller, b_visible );
                tbox_caller.owframe_gtool.setVisible ( true );
            }                        
        };
        
        public void setVisible ( Toolbox tbox_caller, boolean b_visible ) {
            olog_generic.log ( Level.INFO, "Tool.setVisible: " + this + ", " + b_visible );
        }
    }
    
    /** Convenience - just stash it once */
    private static final Tool[] ov_tools = Tool.values ();
    
    /**
     * Public method for javascript to call to popup
     * different applet-based tools.
     *
     * @param s_name should correspond to a member of the Tool enum
     */
    public void setVisible ( String s_name ) {
        for ( Tool n_tool : ov_tools ) {
            if ( n_tool.toString ().equals ( s_name ) ) {
                n_tool.setVisible ( this, true );
                return;
            }
        }
        Tool.LogViewer.setVisible ( this, true );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

